package com.sunnao.spring.ddd.template.client.system.user.req;

import com.sunnao.spring.ddd.template.client.system.user.enums.UserStatusEnum;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 变更用户状态请求DTO
 */
@Getter
@Setter
@ToString
public class ChangeUserStatusRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 目标状态：1-启用，0-禁用 */
    private Integer status;

    /** 操作人ID */
    private Long operatorId;

    @Override
    public ResultDO<Void> check() {
        if (userId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "用户ID不能为空");
        }
        if (UserStatusEnum.getByCode(status) == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "状态取值不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
