package com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 角色-权限关联持久化对象
 * 与 sys_role_permission 表一一对应，硬删除，不继承 BasePO
 */
@Getter
@Setter
@ToString
@Table("sys_role_permission")
public class RolePermissionPO {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;
}
