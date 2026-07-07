package com.sunnao.spring.ddd.template.infrastructure.system.log.converter;

import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.LoginLogEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.LoginLogPO;

import java.util.Collections;
import java.util.List;

/**
 * 登录日志数据转换器
 * 职责：聚合根（内部 LoginLogEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
public class LoginLogConverter {

    private LoginLogConverter() {
    }

    /**
     * PO 转换为聚合根（内部构建 LoginLogEntity）
     */
    public static LoginLogAggregate toAggregate(LoginLogPO po) {
        if (po == null) {
            return null;
        }
        LoginLogEntity entity = new LoginLogEntity();
        entity.setId(po.getId());
        entity.setTraceId(po.getTraceId());
        entity.setUserId(po.getUserId());
        entity.setEmail(po.getEmail());
        entity.setSuccess(po.getSuccess());
        entity.setCode(po.getCode());
        entity.setMsg(po.getMsg());
        entity.setIp(po.getIp());
        entity.setUserAgent(po.getUserAgent());
        entity.setCreateAt(po.getCreateAt());

        LoginLogAggregate aggregate = new LoginLogAggregate();
        aggregate.setLoginLogEntity(entity);
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 LoginLogEntity）
     */
    public static LoginLogPO toPO(LoginLogAggregate aggregate) {
        if (aggregate == null || aggregate.getLoginLogEntity() == null) {
            return null;
        }
        LoginLogEntity entity = aggregate.getLoginLogEntity();
        LoginLogPO po = new LoginLogPO();
        po.setId(entity.getId());
        po.setTraceId(entity.getTraceId());
        po.setUserId(entity.getUserId());
        po.setEmail(entity.getEmail());
        po.setSuccess(entity.getSuccess());
        po.setCode(entity.getCode());
        po.setMsg(entity.getMsg());
        po.setIp(entity.getIp());
        po.setUserAgent(entity.getUserAgent());
        po.setCreateAt(entity.getCreateAt());
        return po;
    }

    /**
     * PO 列表转换为聚合根列表
     */
    public static List<LoginLogAggregate> toAggregateList(List<LoginLogPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(LoginLogConverter::toAggregate).toList();
    }
}
