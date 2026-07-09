package com.sunnao.spring.ddd.template.client.system.dict.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 字典数据 DTO
 */
@Getter
@Setter
@ToString
public class DictDataDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String typeKey;

    private String label;

    private String value;

    private Integer sort;

    private Integer status;

    private String remark;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
