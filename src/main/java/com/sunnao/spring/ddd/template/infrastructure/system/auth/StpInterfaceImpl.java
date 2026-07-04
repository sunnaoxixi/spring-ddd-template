package com.sunnao.spring.ddd.template.infrastructure.system.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据提供实现
 * <p>
 * 角色取自 sys_user.role 字段（admin/user 两级），不做细粒度权限点。
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserRepository userRepository;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 仅做角色鉴权，不使用权限点
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Long userId = Convert.toLong(loginId);
            if (userId == null) {
                return Collections.emptyList();
            }
            UserAggregate aggregate = userRepository.query(userId);
            if (aggregate == null || aggregate.getUserEntity() == null
                    || aggregate.getUserEntity().getRole() == null) {
                return Collections.emptyList();
            }
            return List.of(aggregate.getUserEntity().getRole().getKey());
        } catch (Exception e) {
            log.error("查询用户角色失败, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }
}
