package com.sunnao.spring.ddd.template.client.system.log.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 操作日志 DTO
 */
@Getter
@Setter
@ToString
public class OperLogDTO extends BaseDto {

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
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 请求 URI
     */
    private String uri;

    /**
     * 请求参数摘要
     */
    private String params;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private String resultCode;

    /**
     * 耗时（毫秒）
     */
    private Long costMs;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 操作时间
     */
    private LocalDateTime createAt;
}
