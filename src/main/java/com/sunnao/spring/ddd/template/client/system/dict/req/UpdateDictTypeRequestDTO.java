package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.client.system.dict.enums.DictStatusEnum;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改字典类型请求DTO（typeKey 不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateDictTypeRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    private Long typeId;

    /**
     * 字典类型名称
     */
    private String typeName;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (typeId == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "字典类型ID不能为空");
        }
        boolean typeNameBlank = typeName == null || typeName.isBlank();
        if (typeNameBlank && status == null && remark == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "类型名称、状态、备注不能同时为空");
        }
        if (status != null && DictStatusEnum.getByCode(status) == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "状态取值不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
