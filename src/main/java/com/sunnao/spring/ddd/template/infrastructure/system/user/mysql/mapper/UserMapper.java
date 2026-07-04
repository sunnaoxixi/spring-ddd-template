package com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.po.UserPO;

/**
 * 用户 Mapper 接口（MyBatis-Flex）
 * 仅被 UserRepositoryImpl 调用
 */
public interface UserMapper extends BaseMapper<UserPO> {
}
