package com.sunnao.spring.ddd.template.application.auth.scenario;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.sunnao.spring.ddd.template.application.auth.assembler.AuthAssembler;
import com.sunnao.spring.ddd.template.client.auth.AuthAppService;
import com.sunnao.spring.ddd.template.client.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.auth.req.RegisterRequestDTO;
import com.sunnao.spring.ddd.template.client.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.client.auth.res.RegisterResponseDTO;
import com.sunnao.spring.ddd.template.common.context.LoginSessionKeys;
import com.sunnao.spring.ddd.template.common.context.RequestContextUtils;
import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import com.sunnao.spring.ddd.template.common.filter.TraceIdFilter;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.security.LoginAttemptLimiter;
import com.sunnao.spring.ddd.template.domain.auth.service.AuthDomainService;
import com.sunnao.spring.ddd.template.domain.system.log.event.LoginLogEvent;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.service.UserDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → 领域服务认证 → Sa-Token 签发/注销会话。
 * Sa-Token 调用收敛在应用层，领域层不感知会话技术细节。
 * <p>
 * 登录认证成功/失败均发布 LoginLogEvent 异步落库（参数校验失败不记录）；
 * 登录成功后向 Token-Session 写入 IP/User-Agent/登录时间等附加信息，供在线用户模块展示；
 * 接入 LoginAttemptLimiter 防爆破：凭证失败计数，达限后窗口内拒绝登录，成功即清零。
 */
@Slf4j
@Service
public class AuthAppServiceImpl implements AuthAppService {

    /**
     * 登录成功的结果码（与操作日志约定一致）
     */
    private static final String SUCCESS_CODE = "SUCCESS";

    @Resource
    private AuthDomainService authDomainService;

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private AuthAssembler authAssembler;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Resource
    private LoginAttemptLimiter loginAttemptLimiter;

    @Override
    public ResultDO<LoginResponseDTO> login(LoginRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 防爆破检查：窗口内凭证失败达限则拒绝登录（记录登录日志供审计）
            String clientIp = RequestContextUtils.getClientIp();
            if (loginAttemptLimiter.isBlocked(requestDTO.getEmail(), clientIp)) {
                publishLoginLog(null, requestDTO.getEmail(), false,
                        ErrorCodeEnum.AUTH_LOCKED.getCode(), ErrorCodeEnum.AUTH_LOCKED.getDefaultMsg());
                return ResultDO.buildFailResult(ErrorCodeEnum.AUTH_LOCKED);
            }

            // 3. 领域服务认证（凭证 + 账号状态），成功/失败均记录登录日志
            ResultDO<UserAggregate> domainResult = authDomainService.authenticate(authAssembler.toLoginParam(requestDTO));
            if (!domainResult.isSuccess()) {
                // 仅凭证错误计入失败次数（账号禁用等状态类失败不计）
                if (ErrorCodeEnum.AUTH_FAIL.getCode().equals(domainResult.getCode())) {
                    loginAttemptLimiter.recordFailure(requestDTO.getEmail(), clientIp);
                }
                publishLoginLog(null, requestDTO.getEmail(), false,
                        domainResult.getCode(), domainResult.getMsg());
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }
            loginAttemptLimiter.clear(requestDTO.getEmail(), clientIp);

            // 4. 签发 token（Sa-Token 会话写入 Redis），并向 Token-Session 写入会话附加信息
            UserAggregate aggregate = domainResult.getData();
            Long userId = aggregate.getUserEntity().getId();
            StpUtil.login(userId);
            fillTokenSession(aggregate);
            publishLoginLog(userId, requestDTO.getEmail(), true, SUCCESS_CODE, null);

            // 5. 填充角色标识（RBAC，取自 role 领域）后组装响应
            aggregate.getUserEntity().setRoles(roleRepository.queryRoleKeysByUserId(userId));
            return ResultDO.buildSuccessResult(authAssembler.toLoginResponseDTO(
                    aggregate, StpUtil.getTokenName(), StpUtil.getTokenValue()));
        } catch (Exception e) {
            log.error("登录系统异常, requestDTO: {}", requestDTO, e);
            publishLoginLog(null, requestDTO.getEmail(), false,
                    ErrorCodeEnum.SYSTEM_ERROR.getCode(), ErrorCodeEnum.SYSTEM_ERROR.getDefaultMsg());
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<RegisterResponseDTO> register(RegisterRequestDTO requestDTO) {
        try {
            // 1. 参数自校验（含两次密码一致性校验）
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用用户领域服务创建用户（自助注册：无操作人，默认授予 user 角色）
            //    邮箱唯一性、分布式锁防并发、密码加密、用户创建事件均由领域服务保证
            ResultDO<UserAggregate> domainResult = userDomainService.createUser(
                    authAssembler.toCreateUserParam(requestDTO));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 注册成功后自动登录：签发 token 并写入会话附加信息
            UserAggregate aggregate = domainResult.getData();
            Long userId = aggregate.getUserEntity().getId();
            StpUtil.login(userId);
            fillTokenSession(aggregate);

            // 4. 组装响应（角色标识由 createUser 已填充）
            return ResultDO.buildSuccessResult(authAssembler.toRegisterResponseDTO(
                    aggregate, StpUtil.getTokenName(), StpUtil.getTokenValue()));
        } catch (Exception e) {
            log.error("注册系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 登录成功后向 Token-Session 写入会话附加信息（供在线用户模块展示）；写入失败不影响登录主流程
     */
    private void fillTokenSession(UserAggregate aggregate) {
        try {
            SaSession tokenSession = StpUtil.getTokenSession();
            tokenSession.set(LoginSessionKeys.EMAIL, aggregate.getUserEntity().getEmail());
            tokenSession.set(LoginSessionKeys.NICKNAME, aggregate.getUserEntity().getNickname());
            tokenSession.set(LoginSessionKeys.IP, RequestContextUtils.getClientIp());
            tokenSession.set(LoginSessionKeys.USER_AGENT, RequestContextUtils.getUserAgent());
            tokenSession.set(LoginSessionKeys.LOGIN_TIME, LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error("写入登录会话附加信息失败, userId: {}", aggregate.getUserEntity().getId(), e);
        }
    }

    /**
     * 发布登录日志事件（异步落库）；发布失败不影响登录主流程
     */
    private void publishLoginLog(Long userId, String email, boolean success, String code, String msg) {
        try {
            domainEventPublisher.publish(new LoginLogEvent(
                    MDC.get(TraceIdFilter.TRACE_ID),
                    userId,
                    email,
                    success,
                    code,
                    msg,
                    RequestContextUtils.getClientIp(),
                    RequestContextUtils.getUserAgent()));
        } catch (Exception e) {
            log.error("发布登录日志事件失败, email: {}", email, e);
        }
    }

    @Override
    public ResultDO<Void> logout() {
        try {
            // 未登录时登出视为幂等成功
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
            return ResultDO.buildSuccessResult();
        } catch (Exception e) {
            log.error("登出系统异常", e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
