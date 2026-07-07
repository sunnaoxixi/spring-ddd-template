package com.sunnao.spring.ddd.template.client.system.online.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 分页查询在线用户请求DTO
 */
@Getter
@Setter
@ToString
public class QueryOnlineUserPageRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    @Override
    public ResultDO<Void> check() {
        if (pageNum == null || pageNum < 1) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "页码必须大于等于1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "每页条数必须在1~100之间");
        }
        return ResultDO.buildSuccessResult();
    }
}
