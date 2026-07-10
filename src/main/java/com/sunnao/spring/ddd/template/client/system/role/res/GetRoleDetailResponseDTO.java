package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
/**
 * 获取角色详情响应DTO
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
}
