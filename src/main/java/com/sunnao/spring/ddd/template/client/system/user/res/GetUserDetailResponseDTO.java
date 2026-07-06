package com.sunnao.spring.ddd.template.client.system.user.res;

import com.sunnao.spring.ddd.template.client.system.user.model.UserDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 获取用户详情响应DTO
 */
@Getter
@Setter
@ToString
public class GetUserDetailResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private UserDTO user;
}
