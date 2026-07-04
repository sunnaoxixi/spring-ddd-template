package com.sunnao.spring.ddd.template.common.event;

/**
 * 领域事件发布器接口
 * <p>
 * 定义在 common 层（不依赖 Spring），领域服务可直接注入使用；
 * 具体实现位于 infrastructure 层（SpringDomainEventPublisher）。
 * <p>
 * 发布失败不抛异常、不影响主流程，实现内部自行记录日志。
 */
public interface DomainEventPublisher {

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);
}
