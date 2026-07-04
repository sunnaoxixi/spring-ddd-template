package com.sunnao.spring.ddd.template.client.system.dict.res;

import com.sunnao.spring.ddd.template.client.system.dict.model.DictDataDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 按类型键查询字典数据列表响应DTO
 */
@Getter
@Setter
@ToString
public class QueryDictDataListResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典类型键 */
    private String typeKey;

    /** 字典数据列表（按 sort 升序） */
    private List<DictDataDTO> dataList;
}
