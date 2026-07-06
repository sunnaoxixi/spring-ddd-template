package com.sunnao.spring.ddd.template.client.system.role.model;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 权限 DTO
 * <p>
 * 复用引用对象，权限点由数据库迁移脚本维护，只读。
 */
@Getter
@Setter
@ToString
public class PermissionDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限标识
     */
    private String permKey;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 备注
     */
    private String remark;
}
