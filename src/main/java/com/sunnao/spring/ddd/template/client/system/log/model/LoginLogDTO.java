package com.sunnao.spring.ddd.template.client.system.log.model;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 登录日志 DTO
 */
@Getter
@Setter
@ToString
public class LoginLogDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 用户ID（登录失败时可为空）
     */
    private Long userId;

    /**
     * 登录邮箱
     */
    private String email;

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private String code;

    /**
     * 结果说明（失败原因）
     */
    private String msg;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 客户端 User-Agent
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime createAt;
}
