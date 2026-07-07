package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 创建字典数据请求DTO
 */
@Getter
@Setter
@ToString
public class CreateDictDataRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 排序（升序，可选，默认0）
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (typeKey == null || typeKey.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "字典类型键不能为空");
        }
        if (label == null || label.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "字典标签不能为空");
        }
        if (value == null || value.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "字典值不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
