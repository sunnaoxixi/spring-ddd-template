package com.sunnao.spring.ddd.template.common.exception;

public class AggregateException extends BizException {
    public AggregateException(String code, String msg) {
        super(code, msg);
    }

    public AggregateException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}
