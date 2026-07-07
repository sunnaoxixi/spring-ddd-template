package com.sunnao.spring.ddd.template.client.system.online.model;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 在线用户（会话）DTO
 * <p>
 * 一个用户多端登录时对应多条记录（is-concurrent=true），以 tokenValue 唯一标识一个会话。
 */
@Getter
@Setter
@ToString(exclude = "tokenValue")
public class OnlineUserDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话 token 值（踢人接口以此定位会话；仅管理员接口可见）
     */
    private String tokenValue;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录邮箱
     */
    private String email;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 登录IP
     */
    private String ip;

    /**
     * 登录 User-Agent
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
