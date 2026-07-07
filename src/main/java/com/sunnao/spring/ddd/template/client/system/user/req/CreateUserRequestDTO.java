package com.sunnao.spring.ddd.template.client.system.user.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 创建用户请求DTO
 */
@Getter
@Setter
@ToString(exclude = "password")
public class CreateUserRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码（明文）
     */
    private String password;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色ID集合；为空时默认授予 user 角色
     */
    private List<Long> roleIds;

    @Override
    public ResultDO<Void> check() {
        if (email == null || email.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "邮箱格式不正确");
        }
        if (nickname == null || nickname.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "昵称不能为空");
        }
        if (password == null || password.length() < 6) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "密码长度不能小于6位");
        }
        if (roleIds != null && roleIds.stream().anyMatch(Objects::isNull)) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色ID不能包含空值");
        }
        return ResultDO.buildSuccessResult();
    }
}
