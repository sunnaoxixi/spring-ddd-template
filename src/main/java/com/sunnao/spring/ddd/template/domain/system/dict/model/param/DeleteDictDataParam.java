package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除字典数据参数
 */
@Getter
@Setter
@ToString
public class DeleteDictDataParam extends BaseParam {

    /**
     * 字典数据ID
     */
    private Long dataId;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
