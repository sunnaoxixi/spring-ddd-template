package com.sunnao.spring.ddd.template.domain.system.log.event;

import com.sunnao.spring.ddd.template.common.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

/**
 * 登录日志事件
 * <p>
 * AuthAppServiceImpl 在登录认证成功/失败后发布，
 * 由 application 层 LoginLogListener 异步消费落库（复用 AsyncConfig 线程池）。
 */
@Getter
@ToString(callSuper = true)
public class LoginLogEvent extends DomainEvent {

    /**
     * 链路追踪ID
     */
    private final String traceId;

    /**
     * 登录邮箱
     */
    private final String email;

    /**
     * 是否登录成功
     */
    private final boolean success;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private final String code;

    /**
     * 结果说明（失败原因）
     */
    private final String msg;

    /**
     * 客户端IP
     */
    private final String ip;

    /**
     * 客户端 User-Agent
     */
    private final String userAgent;

    public LoginLogEvent(String traceId, Long userId, String email, boolean success,
                         String code, String msg, String ip, String userAgent) {
        super(userId);
        this.traceId = traceId;
        this.email = email;
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.ip = ip;
        this.userAgent = userAgent;
    }
}
