package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 按类型键查询字典数据列表请求DTO
 */
@Getter
@Setter
@ToString
public class QueryDictDataListRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型键
     */
    private String typeKey;

    @Override
    public ResultDO<Void> check() {
        if (typeKey == null || typeKey.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典类型键不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
