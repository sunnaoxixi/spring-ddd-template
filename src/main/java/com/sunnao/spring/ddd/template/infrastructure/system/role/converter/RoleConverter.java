package com.sunnao.spring.ddd.template.infrastructure.system.role.converter;

import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.PermissionEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.RoleEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.PermissionPO;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.RolePO;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;

import java.util.Collections;
import java.util.List;

/**
 * 角色数据转换器
 * 职责：聚合根（内部 RoleEntity）/ 权限实体与 PO 之间的纯技术转换，无业务逻辑
 */
public class RoleConverter {

    private RoleConverter() {
    }

    /**
     * PO 转换为聚合根（内部构建 RoleEntity，权限 key 集合由仓储按需填充）
     */
    public static RoleAggregate toAggregate(RolePO po) {
        if (po == null) {
            return null;
        }
        RoleEntity entity = new RoleEntity();
        entity.setId(po.getId());
        entity.setRoleKey(po.getRoleKey());
        entity.setRoleName(po.getRoleName());
        // 枚举转换：数据库 Integer → 领域枚举
        entity.setStatus(RoleStatusEnum.getByCode(po.getStatus()));
        entity.setRemark(po.getRemark());
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());

        RoleAggregate aggregate = new RoleAggregate();
        aggregate.setRoleEntity(entity);
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 RoleEntity）
     */
    public static RolePO toPO(RoleAggregate aggregate) {
        if (aggregate == null || aggregate.getRoleEntity() == null) {
            return null;
        }
        RoleEntity entity = aggregate.getRoleEntity();
        RolePO po = new RolePO();
        po.setId(entity.getId());
        po.setRoleKey(entity.getRoleKey());
        po.setRoleName(entity.getRoleName());
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
     * PO 列表转换为聚合根列表
     */
    public static List<RoleAggregate> toAggregateList(List<RolePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(RoleConverter::toAggregate).toList();
    }

    /**
     * 权限 PO 转换为权限实体
     */
    public static PermissionEntity toPermissionEntity(PermissionPO po) {
        if (po == null) {
            return null;
        }
        PermissionEntity entity = new PermissionEntity();
        entity.setId(po.getId());
        entity.setPermKey(po.getPermKey());
        entity.setPermName(po.getPermName());
        entity.setRemark(po.getRemark());
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());
        return entity;
    }

    /**
     * 权限 PO 列表转换为权限实体列表
     */
    public static List<PermissionEntity> toPermissionEntityList(List<PermissionPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(RoleConverter::toPermissionEntity).toList();
    }
}
