package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.client.system.role.model.RoleDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询角色响应DTO
 */
@Getter
@Setter
@ToString
public class QueryRolePageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 角色列表
     */
    private List<RoleDTO> roles;
}
