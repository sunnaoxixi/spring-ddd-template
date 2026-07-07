package com.sunnao.spring.ddd.template.client.system.log.res;

import com.sunnao.spring.ddd.template.client.system.log.model.LoginLogDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询登录日志响应DTO
 */
@Getter
@Setter
@ToString
public class QueryLoginLogPageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 登录日志列表（按登录时间倒序）
     */
    private List<LoginLogDTO> logs;
}
