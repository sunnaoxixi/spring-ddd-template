package com.sunnao.spring.ddd.template.domain.auth.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 登录参数
 */
@Getter
@Setter
@ToString(exclude = "password")
public class LoginParam extends BaseParam {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码（明文，�?DomainService 校验�?
     */
    private String password;
}
