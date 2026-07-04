package com.sunnao.spring.ddd.template.client.system.dict.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改字典类型响应DTO
 */
@Getter
@Setter
@ToString
public class UpdateDictTypeResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典类型ID */
    private Long typeId;
}
