package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除角色请求DTO（逻辑删除）
 */
@Getter
@Setter
@ToString
public class DeleteRoleRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Long roleId;

    @Override
    public ResultDO<Void> check() {
        if (roleId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "角色ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
