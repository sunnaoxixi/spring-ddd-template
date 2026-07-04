package com.sunnao.spring.ddd.template.application.system.log.listener;

import com.sunnao.spring.ddd.template.domain.system.log.event.OperLogEvent;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.repository.OperLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器
 * <p>
 * 异步消费 OperLogAspect 发布的 OperLogEvent（线程池见 AsyncConfig，MDC 已透传），
 * 构建聚合根后落库；消费失败只记录日志，不影响业务主流程。
 */
@Slf4j
@Component
public class OperLogListener {

    @Resource
    private OperLogRepository operLogRepository;

    @Async
    @EventListener
    public void onOperLog(OperLogEvent event) {
        try {
            OperLogAggregate aggregate = OperLogAggregate.create(event);
            operLogRepository.save(aggregate);
        } catch (Exception e) {
            log.error("操作日志落库失败, event: {}", event, e);
        }
    }
}
