package com.sunnao.spring.ddd.template.infrastructure.system.log.converter;

import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.OperLogEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.OperLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志数据转换器
 * 职责：聚合根（内部 OperLogEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface OperLogConverter {

    /**
     * PO 转换为 OperLogEntity（纯字段映射，无枚举转换）
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    OperLogEntity toEntity(OperLogPO po);

    /**
     * OperLogEntity 转换为 PO
     */
    OperLogPO toOperLogPO(OperLogEntity entity);

    /**
     * PO 转换为聚合根（内部构建 OperLogEntity）
     */
    default OperLogAggregate toAggregate(OperLogPO po) {
        if (po == null) {
            return null;
        }
        OperLogAggregate aggregate = new OperLogAggregate();
        aggregate.setOperLogEntity(toEntity(po));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 OperLogEntity）
     */
    default OperLogPO toPO(OperLogAggregate aggregate) {
        if (aggregate == null || aggregate.getOperLogEntity() == null) {
            return null;
        }
        return toOperLogPO(aggregate.getOperLogEntity());
    }

    /**
     * PO 列表转换为聚合根列表
     */
    default List<OperLogAggregate> toAggregateList(List<OperLogPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toAggregate).toList();
    }
}
