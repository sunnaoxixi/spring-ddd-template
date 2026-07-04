package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 给角色分配权限响应DTO
 */
@Getter
@Setter
@ToString
public class AssignPermissionResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Long roleId;
}
