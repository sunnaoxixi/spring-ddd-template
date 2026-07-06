package com.sunnao.spring.ddd.template.common.event;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域事件抽象基类
 * <p>
 * 领域服务在聚合根状态变更持久化成功后，通过 DomainEventPublisher 发布事件，
 * 由 application 层监听器异步消费，实现模块间解耦。
 * 本类不依赖 Spring，具体事件在各业务模块 domain 层定义并继承本类。
 */
@Getter
@ToString
public abstract class DomainEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（全局唯一，用于追踪与幂等）
     */
    private final String eventId;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredAt;

    /**
     * 操作人ID（可为空，如系统触发）
     */
    private final Long operatorId;

    protected DomainEvent(Long operatorId) {
        this.eventId = IdUtil.fastSimpleUUID();
        this.occurredAt = LocalDateTime.now();
        this.operatorId = operatorId;
    }
}
