package com.sunnao.spring.ddd.template.client.system.role.enums;

import lombok.Getter;

/**
 * 角色状态枚举（对外）
 * <p>
 * client 层禁止依赖内部 model 层，故独立定义，
 * 与内部枚举的转换由 application 层 Assembler 完成。
 */
@Getter
public enum RoleStatusEnum {

    /** 启用 */
    ENABLED(1, "启用"),

    /** 禁用 */
    DISABLED(0, "禁用"),
    ;

    private final Integer code;

    private final String description;

    RoleStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     *
     * @param code 码值
     * @return 枚举，未匹配返回 null
     */
    public static RoleStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
