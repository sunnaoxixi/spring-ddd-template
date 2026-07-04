package com.sunnao.spring.ddd.template.client.system.user.model;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 DTO
 * <p>
 * 复用引用对象，被多个 ResponseDTO 引用，不包含密码等敏感字段。
 */
@Getter
@Setter
@ToString
public class UserDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 角色标识集合（如 ["admin","user"]） */
    private List<String> roles;

    /** 头像URL */
    private String avatar;

    /** 创建时间 */
    private LocalDateTime createAt;

    /** 更新时间 */
    private LocalDateTime updateAt;
}
