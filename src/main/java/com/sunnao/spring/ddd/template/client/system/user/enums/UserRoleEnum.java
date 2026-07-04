package com.sunnao.spring.ddd.template.client.system.user.enums;

import lombok.Getter;

/**
 * 用户角色枚举（对外）
 * <p>
 * client 层禁止依赖内部 model 层，故独立定义，
 * 与内部枚举的转换由 application 层 Assembler 完成。
 */
@Getter
public enum UserRoleEnum {

    /** 管理员 */
    ADMIN(1, "管理员"),

    /** 普通用户 */
    USER(0, "普通用户"),
    ;

    private final Integer code;

    private final String description;

    UserRoleEnum(Integer code, String description) {
        this.code = code;
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
