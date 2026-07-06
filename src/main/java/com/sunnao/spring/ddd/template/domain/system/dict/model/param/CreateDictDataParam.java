package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建字典数据参数
 */
@Getter
@Setter
@ToString
public class CreateDictDataParam extends BaseParam {

    /**
     * 字典类型键
     */
    private String typeKey;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    private String value;

    /**
     * 排序（升序，为空时默认0）
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
