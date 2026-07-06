package com.sunnao.spring.ddd.template.application.system.auth.scenario;

import cn.dev33.satoken.stp.StpUtil;
import com.sunnao.spring.ddd.template.application.system.auth.assembler.AuthAssembler;
import com.sunnao.spring.ddd.template.client.system.auth.AuthAppService;
import com.sunnao.spring.ddd.template.client.system.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.system.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.auth.service.AuthDomainService;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → 领域服务认证 → Sa-Token 签发/注销会话。
 * Sa-Token 调用收敛在应用层，领域层不感知会话技术细节。
 */
@Slf4j
@Service
public class AuthAppServiceImpl implements AuthAppService {

    @Resource
    private AuthDomainService authDomainService;

    @Resource
    private RoleRepository roleRepository;

    @Override
    public ResultDO<LoginResponseDTO> login(LoginRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 领域服务认证（凭证 + 账号状态）
            ResultDO<UserAggregate> domainResult = authDomainService.authenticate(AuthAssembler.toLoginParam(requestDTO));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 签发 token（Sa-Token 会话写入 Redis）
            UserAggregate aggregate = domainResult.getData();
            StpUtil.login(aggregate.getUserEntity().getId());

            // 4. 填充角色标识（RBAC，取自 role 领域）后组装响应
            aggregate.getUserEntity().setRoles(
                    roleRepository.queryRoleKeysByUserId(aggregate.getUserEntity().getId()));
            return ResultDO.buildSuccessResult(AuthAssembler.toLoginResponseDTO(
                    aggregate, StpUtil.getTokenName(), StpUtil.getTokenValue()));
        } catch (Exception e) {
            log.error("登录系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
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
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
