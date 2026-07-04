package com.sunnao.spring.ddd.template.domain.system.role.model.entity;

import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限实体
 * <p>
 * 权限点与代码强绑定，由数据库迁移脚本维护种子数据，不提供 CRUD 接口；
 * 属于角色领域，仅作为数据载体（读模式）与分配权限时的校验依据。
 */
@Getter
@Setter
public class PermissionEntity extends BaseEntity {

    /** 权限标识（Sa-Token 鉴权使用，唯一） */
    private String permKey;

    /** 权限名称 */
    private String permName;

    /** 备注 */
    private String remark;
}
