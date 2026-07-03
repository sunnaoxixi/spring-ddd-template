package com.sunnao.spring.ddd.template.common.exception;

public class AdaptorException extends BizException {
    public AdaptorException(String code, String msg) {
        super(code, msg);
    }

    public AdaptorException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}
