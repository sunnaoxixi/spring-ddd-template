package com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户持久化对象
 * 与 sys_user 表一一对应，仅用于 Infrastructure 层内部
 */
@Getter
@Setter
@ToString(exclude = "password")
@Table("sys_user")
public class UserPO {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 角色：1-管理员，0-普通用户 */
    private Integer role;

    /** 头像URL */
    private String avatar;

    /** 创建时间 */
    private LocalDateTime createAt;

    /** 更新时间 */
    private LocalDateTime updateAt;

    /** 创建人ID */
    private Long createBy;

    /** 更新人ID */
    private Long updateBy;

    /** 逻辑删除：0-正常，1-已删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
