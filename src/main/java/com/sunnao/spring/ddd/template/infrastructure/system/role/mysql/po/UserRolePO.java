package com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户-角色关联持久化对象
 * 与 sys_user_role 表一一对应，硬删除，不继承 BasePO
 */
@Getter
@Setter
@ToString
@Table("sys_user_role")
public class UserRolePO {

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;
}
