package com.sunnao.spring.ddd.template.common.model;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public abstract class BaseDto implements Serializable {

    /**
     * 参数自校验
     * <p>
     * RequestDTO 按需覆写本方法实现参数自校验，禁止在 AppService 中编写校验逻辑。
     * 校验不通过时返回 ResultDO.buildFailResult(code, msg)。
     *
     * @return 校验结果
     */
    public ResultDO<Void> check() {
        return ResultDO.buildSuccessResult();
    }
}
