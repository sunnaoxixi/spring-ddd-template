package com.sunnao.spring.ddd.template.client.system.user.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改用户资料请求DTO
 */
@Getter
@Setter
@ToString
public class UpdateUserRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    @Override
    public ResultDO<Void> check() {
        if (userId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "用户ID不能为空");
        }
        boolean nicknameBlank = nickname == null || nickname.isBlank();
        boolean avatarBlank = avatar == null || avatar.isBlank();
        if (nicknameBlank && avatarBlank) {
            return ResultDO.buildFailResult("PARAM_ERROR", "昵称与头像不能同时为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
