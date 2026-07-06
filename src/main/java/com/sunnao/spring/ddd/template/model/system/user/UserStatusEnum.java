package com.sunnao.spring.ddd.template.model.system.user;

import lombok.Getter;

/**
 * 用户状态枚举
 * 共享模型：所有内部模块（domain/application/adaptor/infrastructure）都可以使用
 */
@Getter
public enum UserStatusEnum {

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    ;

    private final Integer code;

    private final String description;

    UserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     *
     * @param code 码值
     * @return 枚举，未匹配返回 null
     */
    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
