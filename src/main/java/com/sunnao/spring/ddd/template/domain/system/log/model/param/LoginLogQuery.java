package com.sunnao.spring.ddd.template.domain.system.log.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件
 */
@Getter
@Setter
@ToString
public class LoginLogQuery extends BaseParam {

    /**
     * 登录邮箱（精确匹配）
     */
    private String email;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 登录时间下限（含）
     */
    private LocalDateTime startTime;

    /**
     * 登录时间上限（含）
     */
    private LocalDateTime endTime;
}
