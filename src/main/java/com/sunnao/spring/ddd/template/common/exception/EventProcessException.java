package com.sunnao.spring.ddd.template.common.exception;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;

public class EventProcessException extends BizException {
    public EventProcessException(String code, String msg) {
        super(code, msg);
    }

    public EventProcessException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public EventProcessException(ErrorCodeEnum errorCode, String msg) {
        super(errorCode, msg);
    }

    public EventProcessException(ErrorCodeEnum errorCode, String msg, Throwable cause) {
        super(errorCode, msg, cause);
    }
}
