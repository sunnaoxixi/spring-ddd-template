package com.sunnao.spring.ddd.template.client.system.online.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询在线用户响应DTO
 */
@Getter
@Setter
@ToString
public class QueryOnlineUserPageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 在线会话总数
     */
    private Long total;

    /**
     * 在线用户（会话）列表
     */
    private List<OnlineUserDTO> onlineUsers;
}
