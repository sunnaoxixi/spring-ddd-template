package com.sunnao.spring.ddd.template.client.system.log.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 分页查询登录日志请求DTO
 */
@Getter
@Setter
@ToString
public class QueryLoginLogPageRequestDTO extends BaseDto {

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

    /**
     * 登录邮箱（精确匹配，可选）
     */
    private String email;

    /**
     * 用户ID（可选）
     */
    private Long userId;

    /**
     * 是否登录成功（可选）
     */
    private Boolean success;

    /**
     * 登录时间下限（含，可选，ISO 格式）
     */
    private LocalDateTime startTime;

    /**
     * 登录时间上限（含，可选，ISO 格式）
     */
    private LocalDateTime endTime;

    @Override
    public ResultDO<Void> check() {
        if (pageNum == null || pageNum < 1) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "页码必须大于等于1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "每页条数必须在1~100之间");
        }
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "时间范围不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
