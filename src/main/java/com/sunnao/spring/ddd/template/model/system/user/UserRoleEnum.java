package com.sunnao.spring.ddd.template.model.system.user;

import lombok.Getter;

/**
 * 用户角色枚举
 * 共享模型：所有内部模块（domain/application/adaptor/infrastructure）都可以使用
 */
@Getter
public enum UserRoleEnum {

    /** 管理员 */
    ADMIN(1, "admin", "管理员"),

    /** 普通用户 */
    USER(0, "user", "普通用户"),
    ;

    /** 码值（数据库存储） */
    private final Integer code;

    /** 角色标识（Sa-Token 鉴权使用） */
    private final String key;

    private final String description;

    UserRoleEnum(Integer code, String key, String description) {
        this.code = code;
        this.key = key;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     *
     * @param code 码值
     * @return 枚举，未匹配返回 null
     */
    public static UserRoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
