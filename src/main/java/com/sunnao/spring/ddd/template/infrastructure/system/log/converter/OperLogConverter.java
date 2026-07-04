package com.sunnao.spring.ddd.template.infrastructure.system.log.converter;

import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.OperLogEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.OperLogPO;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志数据转换器
 * 职责：聚合根（内部 OperLogEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
public class OperLogConverter {

    private OperLogConverter() {
    }

    /**
     * PO 转换为聚合根（内部构建 OperLogEntity）
     */
    public static OperLogAggregate toAggregate(OperLogPO po) {
        if (po == null) {
            return null;
        }
        OperLogEntity entity = new OperLogEntity();
        entity.setId(po.getId());
        entity.setTraceId(po.getTraceId());
        entity.setOperatorId(po.getOperatorId());
        entity.setModule(po.getModule());
        entity.setAction(po.getAction());
        entity.setUri(po.getUri());
        entity.setParams(po.getParams());
        entity.setResultCode(po.getResultCode());
        entity.setCostMs(po.getCostMs());
        entity.setIp(po.getIp());
        entity.setCreateAt(po.getCreateAt());

        OperLogAggregate aggregate = new OperLogAggregate();
        aggregate.setOperLogEntity(entity);
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 OperLogEntity）
     */
    public static OperLogPO toPO(OperLogAggregate aggregate) {
        if (aggregate == null || aggregate.getOperLogEntity() == null) {
            return null;
        }
        OperLogEntity entity = aggregate.getOperLogEntity();
        OperLogPO po = new OperLogPO();
        po.setId(entity.getId());
        po.setTraceId(entity.getTraceId());
        po.setOperatorId(entity.getOperatorId());
        po.setModule(entity.getModule());
        po.setAction(entity.getAction());
        po.setUri(entity.getUri());
        po.setParams(entity.getParams());
        po.setResultCode(entity.getResultCode());
        po.setCostMs(entity.getCostMs());
        po.setIp(entity.getIp());
        po.setCreateAt(entity.getCreateAt());
        return po;
    }

    /**
     * PO 列表转换为聚合根列表
     */
    public static List<OperLogAggregate> toAggregateList(List<OperLogPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(OperLogConverter::toAggregate).toList();
    }
}
