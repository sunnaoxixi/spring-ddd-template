package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 获取角色详情请求DTO
 */
@Getter
@Setter
@ToString
public class GetRoleDetailRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    @Override
    public ResultDO<Void> check() {
        if (roleId == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
