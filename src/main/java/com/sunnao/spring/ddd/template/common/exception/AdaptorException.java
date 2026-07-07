package com.sunnao.spring.ddd.template.common.exception;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;

public class AdaptorException extends BizException {
    public AdaptorException(String code, String msg) {
        super(code, msg);
    }

    public AdaptorException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public AdaptorException(ErrorCodeEnum errorCode, String msg) {
        super(errorCode, msg);
    }

    public AdaptorException(ErrorCodeEnum errorCode, String msg, Throwable cause) {
        super(errorCode, msg, cause);
    }
}
