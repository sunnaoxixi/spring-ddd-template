package com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.LoginLogPO;

/**
 * 登录日志 Mapper 接口（MyBatis-Flex）
 * 仅被 LoginLogRepositoryImpl 调用
 */
public interface LoginLogMapper extends BaseMapper<LoginLogPO> {
}
