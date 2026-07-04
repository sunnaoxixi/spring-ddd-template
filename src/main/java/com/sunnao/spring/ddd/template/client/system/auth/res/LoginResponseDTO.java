package com.sunnao.spring.ddd.template.client.system.auth.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 登录响应DTO
 */
@Getter
@Setter
@ToString
public class LoginResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** token 名称（后续请求以此为请求头名称携带 token） */
    private String tokenName;

    /** token 值 */
    private String tokenValue;

    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 角色：1-管理员，0-普通用户 */
    private Integer role;
}
