package com.sunnao.spring.ddd.template.domain.system.log.event;

import com.sunnao.spring.ddd.template.common.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

/**
 * 操作日志事件
 * <p>
 * OperLogAspect 采集写接口执行信息后发布，
 * 由 application 层 OperLogListener 异步消费落库（复用 AsyncConfig 线程池）。
 */
@Getter
@ToString(callSuper = true)
public class OperLogEvent extends DomainEvent {

    /** 链路追踪ID */
    private final String traceId;

    /** 业务模块 */
    private final String module;

    /** 操作动作 */
    private final String action;

    /** 请求 URI */
    private final String uri;

    /** 请求参数摘要（超长截断） */
    private final String params;

    /** 结果码（SUCCESS 或错误码） */
    private final String resultCode;

    /** 耗时（毫秒） */
    private final Long costMs;

    /** 客户端IP */
    private final String ip;

    public OperLogEvent(String traceId, Long operatorId, String module, String action,
                        String uri, String params, String resultCode, Long costMs, String ip) {
        super(operatorId);
        this.traceId = traceId;
        this.module = module;
        this.action = action;
        this.uri = uri;
        this.params = params;
        this.resultCode = resultCode;
        this.costMs = costMs;
        this.ip = ip;
    }
}
