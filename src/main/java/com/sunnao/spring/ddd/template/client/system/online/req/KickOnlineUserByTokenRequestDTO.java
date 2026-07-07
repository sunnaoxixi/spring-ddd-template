package com.sunnao.spring.ddd.template.client.system.online.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 按会话 token 踢人请求DTO
 */
@Getter
@Setter
@ToString(exclude = "tokenValue")
public class KickOnlineUserByTokenRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话 token 值
     */
    private String tokenValue;

    @Override
    public ResultDO<Void> check() {
        if (tokenValue == null || tokenValue.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "会话token不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
