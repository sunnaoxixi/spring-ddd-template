package com.sunnao.spring.ddd.template.client.system.log.res;

import com.sunnao.spring.ddd.template.client.system.log.model.OperLogDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询操作日志响应DTO
 */
@Getter
@Setter
@ToString
public class QueryOperLogPageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总条数 */
    private Long total;

    /** 操作日志列表（按操作时间倒序） */
    private List<OperLogDTO> logs;
}
