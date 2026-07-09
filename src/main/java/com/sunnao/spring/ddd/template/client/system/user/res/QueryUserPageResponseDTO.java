package com.sunnao.spring.ddd.template.client.system.user.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询用户响应DTO
 */
@Getter
@Setter
@ToString
public class QueryUserPageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 用户列表
     */
    private List<UserDTO> users;
}
