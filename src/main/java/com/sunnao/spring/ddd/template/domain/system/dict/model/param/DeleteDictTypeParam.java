package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除字典类型参数
 */
@Getter
@Setter
@ToString
public class DeleteDictTypeParam extends BaseParam {

    /**
     * 字典类型ID
     */
    private Long typeId;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
