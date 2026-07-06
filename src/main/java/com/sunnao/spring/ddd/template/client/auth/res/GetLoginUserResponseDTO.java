package com.sunnao.spring.ddd.template.client.system.auth.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 当前登录用户信息响应DTO
 */
@Getter
@Setter
@ToString
public class GetLoginUserResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 角色标识集合（如 ["admin","user"]） */
    private List<String> roles;

    /** 状态：1-启用，0-禁用 */
    private Integer status;
}
