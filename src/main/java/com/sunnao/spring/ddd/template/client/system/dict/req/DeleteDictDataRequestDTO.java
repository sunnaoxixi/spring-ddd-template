package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除字典数据请求DTO（逻辑删除）
 */
@Getter
@Setter
@ToString
public class DeleteDictDataRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典数据ID
     */
    private Long dataId;

    @Override
    public ResultDO<Void> check() {
        if (dataId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典数据ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
