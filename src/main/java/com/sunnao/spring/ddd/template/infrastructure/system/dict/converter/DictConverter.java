package com.sunnao.spring.ddd.template.infrastructure.system.dict.converter;

import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictDataEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictTypeEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictDataPO;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictTypePO;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;

import java.util.Collections;
import java.util.List;

/**
 * 字典数据转换器
 * 职责：聚合根（内部实体）与 PO 之间的纯技术转换，无业务逻辑
 */
public class DictConverter {

    private DictConverter() {
    }

    /**
     * 字典类型 PO 转换为聚合根
     */
    public static DictTypeAggregate toTypeAggregate(DictTypePO po) {
        if (po == null) {
            return null;
        }
        DictTypeEntity entity = new DictTypeEntity();
        entity.setId(po.getId());
        entity.setTypeKey(po.getTypeKey());
        entity.setTypeName(po.getTypeName());
        // 枚举转换：数据库 Integer → 领域枚举
        entity.setStatus(DictStatusEnum.getByCode(po.getStatus()));
        entity.setRemark(po.getRemark());
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());

        DictTypeAggregate aggregate = new DictTypeAggregate();
        aggregate.setDictTypeEntity(entity);
        return aggregate;
    }

    /**
     * 字典类型聚合根转换为 PO
     */
    public static DictTypePO toTypePO(DictTypeAggregate aggregate) {
        if (aggregate == null || aggregate.getDictTypeEntity() == null) {
            return null;
        }
        DictTypeEntity entity = aggregate.getDictTypeEntity();
        DictTypePO po = new DictTypePO();
        po.setId(entity.getId());
        po.setTypeKey(entity.getTypeKey());
        po.setTypeName(entity.getTypeName());
        // 枚举转换：领域枚举 → 数据库 Integer
        if (entity.getStatus() != null) {
            po.setStatus(entity.getStatus().getCode());
        }
        po.setRemark(entity.getRemark());
        po.setCreateAt(entity.getCreateAt());
        po.setUpdateAt(entity.getUpdateAt());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        return po;
    }

    /**
     * 字典类型 PO 列表转换为聚合根列表
     */
    public static List<DictTypeAggregate> toTypeAggregateList(List<DictTypePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(DictConverter::toTypeAggregate).toList();
    }

    /**
     * 字典数据 PO 转换为聚合根
     */
    public static DictDataAggregate toDataAggregate(DictDataPO po) {
        if (po == null) {
            return null;
        }
        DictDataEntity entity = new DictDataEntity();
        entity.setId(po.getId());
        entity.setTypeKey(po.getTypeKey());
        entity.setLabel(po.getLabel());
        entity.setValue(po.getDictValue());
        entity.setSort(po.getSort());
        // 枚举转换：数据库 Integer → 领域枚举
        entity.setStatus(DictStatusEnum.getByCode(po.getStatus()));
        entity.setRemark(po.getRemark());
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());

        DictDataAggregate aggregate = new DictDataAggregate();
        aggregate.setDictDataEntity(entity);
        return aggregate;
    }

    /**
     * 字典数据聚合根转换为 PO
     */
    public static DictDataPO toDataPO(DictDataAggregate aggregate) {
        if (aggregate == null || aggregate.getDictDataEntity() == null) {
            return null;
        }
        DictDataEntity entity = aggregate.getDictDataEntity();
        DictDataPO po = new DictDataPO();
        po.setId(entity.getId());
        po.setTypeKey(entity.getTypeKey());
        po.setLabel(entity.getLabel());
        po.setDictValue(entity.getValue());
        po.setSort(entity.getSort());
        // 枚举转换：领域枚举 → 数据库 Integer
        if (entity.getStatus() != null) {
            po.setStatus(entity.getStatus().getCode());
        }
        po.setRemark(entity.getRemark());
        po.setCreateAt(entity.getCreateAt());
        po.setUpdateAt(entity.getUpdateAt());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        return po;
    }

    /**
     * 字典数据 PO 列表转换为聚合根列表
     */
    public static List<DictDataAggregate> toDataAggregateList(List<DictDataPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(DictConverter::toDataAggregate).toList();
    }
}
