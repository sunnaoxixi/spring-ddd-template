package com.sunnao.spring.ddd.template.client.system.user.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 变更用户状态响应DTO
 */
@Getter
@Setter
@ToString
public class ChangeUserStatusResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变更后状态：1-启用，0-禁用
     */
    private Integer status;
}
