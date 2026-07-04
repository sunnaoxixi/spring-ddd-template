package com.sunnao.spring.ddd.template.domain.system.user.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建用户参数
 */
@Getter
@Setter
@ToString(exclude = "password")
public class CreateUserParam extends BaseParam {

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 密码（明文，由 DomainService 加密） */
    private String password;

    /** 头像URL */
    private String avatar;

    /** 操作人ID */
    private Long operatorId;
}
