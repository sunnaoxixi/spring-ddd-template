package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字典类型查询条件
 */
@Getter
@Setter
@ToString
public class DictTypeQuery extends BaseParam {

    /** 字典类型键（精确匹配） */
    private String typeKey;

    /** 字典类型名称（模糊匹配） */
    private String typeName;

    /** 状态 */
    private DictStatusEnum status;
}
