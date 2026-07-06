package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 修改字典类型参数（typeKey 创建后不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateDictTypeParam extends BaseParam {

    /**
     * 字典类型ID
     */
    private Long typeId;

    /**
     * 字典类型名称
     */
    private String typeName;

    /**
     * 状态
     */
    private DictStatusEnum status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
