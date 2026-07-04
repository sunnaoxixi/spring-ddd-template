package com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.OperLogPO;

/**
 * 操作日志 Mapper 接口（MyBatis-Flex）
 * 仅被 OperLogRepositoryImpl 调用
 */
public interface OperLogMapper extends BaseMapper<OperLogPO> {
}
