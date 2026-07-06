package com.sunnao.spring.ddd.template.client.auth.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.regex.Pattern;

/**
 * 登录请求DTO
 */
@Getter
@Setter
@ToString(exclude = "password")
public class LoginRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码（明文）
     */
    private String password;

    @Override
    public ResultDO<Void> check() {
        if (email == null || email.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "邮箱格式不正�?);
        }
        if (password == null || password.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "密码不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
