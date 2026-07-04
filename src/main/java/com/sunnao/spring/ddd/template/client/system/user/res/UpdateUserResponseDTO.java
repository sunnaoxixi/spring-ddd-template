package com.sunnao.spring.ddd.template.client.system.user.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改用户资料响应DTO
 */
@Getter
@Setter
@ToString
public class UpdateUserResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
}
