package com.sunnao.spring.ddd.template.common.exception;

import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;

public class RepositoryException extends BizException {
    public RepositoryException(String code, String msg) {
        super(code, msg);
    }

    public RepositoryException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public RepositoryException(ErrorCodeEnum errorCode, String msg) {
        super(errorCode, msg);
    }

    public RepositoryException(ErrorCodeEnum errorCode, String msg, Throwable cause) {
        super(errorCode, msg, cause);
    }
}
