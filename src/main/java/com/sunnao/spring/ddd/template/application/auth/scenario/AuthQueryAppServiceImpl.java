package com.sunnao.spring.ddd.template.application.system.auth.scenario;

import cn.dev33.satoken.stp.StpUtil;
import com.sunnao.spring.ddd.template.application.system.auth.assembler.AuthAssembler;
import com.sunnao.spring.ddd.template.client.system.auth.AuthQueryAppService;
import com.sunnao.spring.ddd.template.client.system.auth.res.GetLoginUserResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证查询应用服务实现（读模式）
 * 职责：从 Sa-Token 会话取登录ID，通过 Repository 获取聚合根后经 Assembler 转换为 DTO
 */
@Slf4j
@Service
public class AuthQueryAppServiceImpl implements AuthQueryAppService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

    @Override
    public ResultDO<GetLoginUserResponseDTO> getLoginUserInfo() {
        try {
            // 1. 获取当前会话登录ID（路由拦截器已保证登录态，此处兜底校验）
            if (!StpUtil.isLogin()) {
                return ResultDO.buildFailResult("NOT_LOGIN", "未登录");
            }
            long userId = StpUtil.getLoginIdAsLong();

            // 2. 查询本领域用户数据
            UserAggregate aggregate = userRepository.query(userId);
            if (aggregate == null) {
                return ResultDO.buildFailResult("USER_NOT_FOUND", "用户不存在");
            }

            // 3. 填充角色标识（RBAC，取自 role 领域）后组装响应 DTO
            aggregate.getUserEntity().setRoles(roleRepository.queryRoleKeysByUserId(userId));
            return ResultDO.buildSuccessResult(AuthAssembler.toGetLoginUserResponseDTO(aggregate));
        } catch (Exception e) {
            log.error("获取当前登录用户信息失败", e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
