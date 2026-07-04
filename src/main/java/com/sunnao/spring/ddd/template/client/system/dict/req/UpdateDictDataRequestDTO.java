package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.client.system.dict.enums.DictStatusEnum;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改字典数据请求DTO（typeKey 不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateDictDataRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 字典数据ID */
    private Long dataId;

    /** 字典标签 */
    private String label;

    /** 字典值 */
    private String value;

    /** 排序（升序） */
    private Integer sort;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (dataId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典数据ID不能为空");
        }
        boolean labelBlank = label == null || label.isBlank();
        boolean valueBlank = value == null || value.isBlank();
        if (labelBlank && valueBlank && sort == null && status == null && remark == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "修改内容不能全部为空");
        }
        if (status != null && DictStatusEnum.getByCode(status) == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "状态取值不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
