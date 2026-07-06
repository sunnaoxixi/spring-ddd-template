package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 给用户授予角色响应DTO
 */
@Getter
@Setter
@ToString
public class AssignUserRoleResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
}
