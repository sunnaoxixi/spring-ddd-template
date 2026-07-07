package com.sunnao.spring.ddd.template.application.system.log.listener;

import com.sunnao.spring.ddd.template.domain.system.log.event.LoginLogEvent;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.repository.LoginLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件监听器
 * <p>
 * 异步消费 AuthAppServiceImpl 发布的 LoginLogEvent（线程池见 AsyncConfig，MDC 已透传），
 * 构建聚合根后落库；消费失败只记录日志，不影响登录主流程。
 */
@Slf4j
@Component
public class LoginLogListener {

    @Resource
    private LoginLogRepository loginLogRepository;

    @Async
    @EventListener
    public void onLoginLog(LoginLogEvent event) {
        try {
            LoginLogAggregate aggregate = LoginLogAggregate.create(event);
            loginLogRepository.save(aggregate);
        } catch (Exception e) {
            log.error("登录日志落库失败, event: {}", event, e);
        }
    }
}
