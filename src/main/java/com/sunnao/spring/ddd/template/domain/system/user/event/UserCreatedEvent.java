package com.sunnao.spring.ddd.template.domain.system.user.event;

import com.sunnao.spring.ddd.template.common.event.DomainEvent;
import lombok.Getter;
import lombok.ToString;

/**
 * 用户创建领域事件
 * <p>
 * UserDomainServiceImpl.createUser 持久化成功后发布，
 * 由 application 层 UserCreatedListener 异步消费。
 */
@Getter
@ToString(callSuper = true)
public class UserCreatedEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 邮箱
     */
    private final String email;

    /**
     * 昵称
     */
    private final String nickname;

    public UserCreatedEvent(Long userId, String email, String nickname, Long operatorId) {
        super(operatorId);
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }
}
