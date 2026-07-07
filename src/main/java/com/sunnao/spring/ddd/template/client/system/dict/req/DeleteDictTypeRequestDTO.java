package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除字典类型请求DTO（逻辑删除，同时删除其下字典数据）
 */
@Getter
@Setter
@ToString
public class DeleteDictTypeRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    private Long typeId;

    @Override
    public ResultDO<Void> check() {
        if (typeId == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "字典类型ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
