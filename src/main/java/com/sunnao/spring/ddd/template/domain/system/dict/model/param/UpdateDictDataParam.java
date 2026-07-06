package com.sunnao.spring.ddd.template.domain.system.dict.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 修改字典数据参数（typeKey 创建后不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateDictDataParam extends BaseParam {

    /**
     * 字典数据ID
     */
    private Long dataId;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    private String value;

    /**
     * 排序（升序）
     */
    private Integer sort;

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
