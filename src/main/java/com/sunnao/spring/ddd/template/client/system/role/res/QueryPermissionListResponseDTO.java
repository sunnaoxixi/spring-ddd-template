package com.sunnao.spring.ddd.template.client.system.role.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 查询全部权限点响应DTO
 */
@Getter
@Setter
@ToString
public class QueryPermissionListResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限点列表
     */
    private List<PermissionDTO> permissions;
}
