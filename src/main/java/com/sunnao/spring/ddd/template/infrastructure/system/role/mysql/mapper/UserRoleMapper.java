package com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.UserRolePO;

/**
 * 用户-角色关联 Mapper 接口（MyBatis-Flex）
 * 仅被 RoleRepositoryImpl 调用
 */
public interface UserRoleMapper extends BaseMapper<UserRolePO> {
}
