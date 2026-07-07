package com.sunnao.spring.ddd.template.common.exception;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;

public class AggregateException extends BizException {
    public AggregateException(String code, String msg) {
        super(code, msg);
    }

    public AggregateException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public AggregateException(ErrorCodeEnum errorCode, String msg) {
        super(errorCode, msg);
    }

    public AggregateException(ErrorCodeEnum errorCode, String msg, Throwable cause) {
        super(errorCode, msg, cause);
    }
}
