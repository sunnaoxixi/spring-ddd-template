package com.sunnao.spring.ddd.template.infrastructure.system.dict.converter;

import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictDataEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictTypeEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictDataPO;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictTypePO;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 字典数据转换器
 * 职责：聚合根（内部实体）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface DictConverter {

    // ========== 字典类型转换 ==========

    /**
     * 字典类型 PO 转换为 DictTypeEntity（枚举转换：数据库 Integer → 领域枚举）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "intToDictStatus")
    DictTypeEntity toTypeEntity(DictTypePO po);

    /**
     * DictTypeEntity 转换为 PO（枚举转换：领域枚举 → 数据库 Integer）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "dictStatusToInt")
    @Mapping(target = "deleted", ignore = true)
    DictTypePO toTypePOFromEntity(DictTypeEntity entity);

    /**
     * 字典类型 PO 转换为聚合根
     */
    default DictTypeAggregate toTypeAggregate(DictTypePO po) {
        if (po == null) {
            return null;
        }
        DictTypeAggregate aggregate = new DictTypeAggregate();
        aggregate.setDictTypeEntity(toTypeEntity(po));
        return aggregate;
    }

    /**
     * 字典类型聚合根转换为 PO
     */
    default DictTypePO toTypePO(DictTypeAggregate aggregate) {
        if (aggregate == null || aggregate.getDictTypeEntity() == null) {
            return null;
        }
        return toTypePOFromEntity(aggregate.getDictTypeEntity());
    }

    /**
     * 字典类型 PO 列表转换为聚合根列表
     */
    default List<DictTypeAggregate> toTypeAggregateList(List<DictTypePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toTypeAggregate).toList();
    }

    // ========== 字典数据转换 ==========

    /**
     * 字典数据 PO 转换为 DictDataEntity
     * 注意字段名差异：PO.dictValue → Entity.value
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "intToDictStatus")
    @Mapping(target = "value", source = "dictValue")
    DictDataEntity toDataEntity(DictDataPO po);

    /**
     * DictDataEntity 转换为 PO
     * 注意字段名差异：Entity.value → PO.dictValue
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "dictStatusToInt")
    @Mapping(target = "dictValue", source = "value")
    @Mapping(target = "deleted", ignore = true)
    DictDataPO toDataPOFromEntity(DictDataEntity entity);

    /**
     * 字典数据 PO 转换为聚合根
     */
    default DictDataAggregate toDataAggregate(DictDataPO po) {
        if (po == null) {
            return null;
        }
        DictDataAggregate aggregate = new DictDataAggregate();
        aggregate.setDictDataEntity(toDataEntity(po));
        return aggregate;
    }

    /**
     * 字典数据聚合根转换为 PO
     */
    default DictDataPO toDataPO(DictDataAggregate aggregate) {
        if (aggregate == null || aggregate.getDictDataEntity() == null) {
            return null;
        }
        return toDataPOFromEntity(aggregate.getDictDataEntity());
    }

    /**
     * 字典数据 PO 列表转换为聚合根列表
     */
    default List<DictDataAggregate> toDataAggregateList(List<DictDataPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toDataAggregate).toList();
    }

    // ========== 枚举转换辅助方法 ==========

    /**
     * 数据库 Integer → 领域枚举
     */
    @Named("intToDictStatus")
    default DictStatusEnum intToDictStatus(Integer code) {
        return DictStatusEnum.getByCode(code);
    }

    /**
     * 领域枚举 → 数据库 Integer
     */
    @Named("dictStatusToInt")
    default Integer dictStatusToInt(DictStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
