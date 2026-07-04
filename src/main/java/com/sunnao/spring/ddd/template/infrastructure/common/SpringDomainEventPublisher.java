package com.sunnao.spring.ddd.template.infrastructure.common;

import com.sunnao.spring.ddd.template.common.event.DomainEvent;
import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器 Spring 实现
 * <p>
 * 基于 ApplicationEventPublisher 进程内广播，监听器以 @Async 异步消费；
 * 发布失败仅记录日志，不影响主流程。
 */
@Slf4j
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        try {
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("发布领域事件失败, event: {}", event, e);
        }
    }
}
