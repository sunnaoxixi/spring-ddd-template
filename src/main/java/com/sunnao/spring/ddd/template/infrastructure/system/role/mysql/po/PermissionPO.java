package com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.sunnao.spring.ddd.template.common.model.BasePO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限持久化对象
 * 与 sys_permission 表一一对应，仅用于 Infrastructure 层内部；
 * 权限点由迁移脚本维护种子数据，代码侧只读
 */
@Getter
@Setter
@ToString
@Table("sys_permission")
public class PermissionPO extends BasePO {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 权限标识（Sa-Token 鉴权使用，唯一） */
    private String permKey;

    /** 权限名称 */
    private String permName;

    /** 备注 */
    private String remark;

    /** 逻辑删除：0-正常，1-已删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
