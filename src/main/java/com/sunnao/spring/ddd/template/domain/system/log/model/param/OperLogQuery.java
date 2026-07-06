package com.sunnao.spring.ddd.template.domain.system.log.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 操作日志查询条件
 */
@Getter
@Setter
@ToString
public class OperLogQuery extends BaseParam {

    /**
     * 业务模块（精确匹配）
     */
    private String module;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间下限（含）
     */
    private LocalDateTime startTime;

    /**
     * 操作时间上限（含）
     */
    private LocalDateTime endTime;
}
