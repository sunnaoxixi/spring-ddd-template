package com.sunnao.spring.ddd.template.client.system.user.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除用户请求DTO（逻辑删除）
 */
@Getter
@Setter
@ToString
public class DeleteUserRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    @Override
    public ResultDO<Void> check() {
        if (userId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "用户ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
