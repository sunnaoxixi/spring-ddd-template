package com.sunnao.spring.ddd.template.common.exception;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import lombok.Getter;

@Getter
public class BizException extends Exception {
    private final String code;

    public BizException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(String code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public BizException(ErrorCodeEnum errorCode, String msg) {
        this(errorCode.getCode(), msg);
    }

    public BizException(ErrorCodeEnum errorCode, String msg, Throwable cause) {
        this(errorCode.getCode(), msg, cause);
    }
}
