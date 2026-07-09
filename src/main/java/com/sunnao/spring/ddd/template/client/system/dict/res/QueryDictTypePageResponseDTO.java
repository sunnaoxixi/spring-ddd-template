package com.sunnao.spring.ddd.template.client.system.dict.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询字典类型响应DTO
 */
@Getter
@Setter
@ToString
public class QueryDictTypePageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 字典类型列表
     */
    private List<DictTypeDTO> types;
}
