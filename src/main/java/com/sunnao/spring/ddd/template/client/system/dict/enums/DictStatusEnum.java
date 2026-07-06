package com.sunnao.spring.ddd.template.client.system.dict.enums;

import lombok.Getter;

/**
 * 字典状态枚举（对外）
 */
@Getter
public enum DictStatusEnum {

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

    DictStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DictStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DictStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
