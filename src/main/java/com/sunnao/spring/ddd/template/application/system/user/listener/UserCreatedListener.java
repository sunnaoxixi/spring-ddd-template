package com.sunnao.spring.ddd.template.application.system.user.listener;

import com.sunnao.spring.ddd.template.domain.system.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户创建事件监听器（示例）
 * <p>
 * 异步消费 UserCreatedEvent（线程池见 AsyncConfig，MDC 已透传），
 * 示例仅记录日志，可扩展为发送欢迎邮件、初始化用户配置等；
 * 消费失败只记录日志，不影响主流程。
 */
@Slf4j
@Component
public class UserCreatedListener {

    @Async
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        try {
            log.info("消费用户创建事件, eventId: {}, userId: {}, email: {}, operatorId: {}",
                    event.getEventId(), event.getUserId(), event.getEmail(), event.getOperatorId());
        } catch (Exception e) {
            log.error("消费用户创建事件失败, event: {}", event, e);
        }
    }
}
