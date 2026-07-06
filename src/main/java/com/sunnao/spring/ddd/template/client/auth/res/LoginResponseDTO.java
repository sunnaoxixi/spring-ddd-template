package com.sunnao.spring.ddd.template.client.auth.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 登录响应DTO
 */
@Getter
@Setter
@ToString
public class LoginResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * token 名称（后续请求以此为请求头名称携�?token�?
     */
    private String tokenName;

    /**
     * token �?
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
     * 角色标识集合（如 ["admin","user"]�?
     */
    private List<String> roles;
}
