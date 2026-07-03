package com.sunnao.spring.ddd.template.common.exception;

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
}
