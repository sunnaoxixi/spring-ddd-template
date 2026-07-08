package com.sunnao.spring.ddd.template.client.auth.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 注册响应DTO
 * <p>
 * 注册成功后自动登录，返回 token 与用户基础信息。
 */
@Getter
@Setter
@ToString
public class RegisterResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * token 名称（后续请求以此为请求头名称携带 token）
     */
    private String tokenName;

    /**
     * token 值
     */
    private String tokenValue;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色标识集合（如 ["user"]）
     */
    private List<String> roles;
}
