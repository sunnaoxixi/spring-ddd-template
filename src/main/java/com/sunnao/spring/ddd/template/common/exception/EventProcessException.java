package com.sunnao.spring.ddd.template.common.exception;

public class EventProcessException extends BizException {
    public EventProcessException(String code, String msg) {
        super(code, msg);
    }

    public EventProcessException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}
