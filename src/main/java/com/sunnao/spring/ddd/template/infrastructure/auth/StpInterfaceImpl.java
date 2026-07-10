package com.sunnao.spring.ddd.template.infrastructure.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色数据提供实现
 * <p>
 * 角色取自 sys_role / sys_user_role，仅统计启用状态角色；
 * 查询失败降级为空集合（表现为无可用角色）。
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RoleRepository roleRepository;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Long userId = Convert.toLong(loginId);
            if (userId == null) {
                return Collections.emptyList();
            }
            return roleRepository.queryRoleKeysByUserId(userId);
        } catch (Exception e) {
            log.error("查询用户角色失败, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }
}
