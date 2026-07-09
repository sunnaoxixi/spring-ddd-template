package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 获取角色详情响应DTO（含权限 key 集合）
 */
@Getter
@Setter
@ToString
public class GetRoleDetailResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色信息
     */
    private RoleDTO role;

    /**
     * 角色拥有的权限标识集合
     */
    private List<String> permKeys;
}
