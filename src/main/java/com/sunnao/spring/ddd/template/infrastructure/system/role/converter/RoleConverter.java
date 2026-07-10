package com.sunnao.spring.ddd.template.infrastructure.system.role.converter;

import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.RoleEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.RolePO;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 角色数据转换器
 * 职责：聚合根（内部 RoleEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface RoleConverter {

    /**
     * PO 转换为 RoleEntity（枚举转换：数据库 Integer → 领域枚举）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "intToRoleStatus")
    RoleEntity toEntity(RolePO po);

    /**
     * RoleEntity 转换为 PO（枚举转换：领域枚举 → 数据库 Integer）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "roleStatusToInt")
    @Mapping(target = "deleted", ignore = true)
    RolePO toRolePO(RoleEntity entity);

    /**
     * PO 转换为聚合根（内部构建 RoleEntity）
     */
    default RoleAggregate toAggregate(RolePO po) {
        if (po == null) {
            return null;
        }
        RoleAggregate aggregate = new RoleAggregate();
        aggregate.setRoleEntity(toEntity(po));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 RoleEntity）
     */
    default RolePO toPO(RoleAggregate aggregate) {
        if (aggregate == null || aggregate.getRoleEntity() == null) {
            return null;
        }
        return toRolePO(aggregate.getRoleEntity());
    }

    /**
     * PO 列表转换为聚合根列表
     */
    default List<RoleAggregate> toAggregateList(List<RolePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toAggregate).toList();
    }

    // ========== 枚举转换辅助方法 ==========

    /**
     * 数据库 Integer → 领域枚举
     */
    @Named("intToRoleStatus")
    default RoleStatusEnum intToRoleStatus(Integer code) {
        return RoleStatusEnum.getByCode(code);
    }

    /**
     * 领域枚举 → 数据库 Integer
     */
    @Named("roleStatusToInt")
    default Integer roleStatusToInt(RoleStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
