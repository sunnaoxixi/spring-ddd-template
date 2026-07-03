package com.sunnao.spring.ddd.template.common.exception;

public class RepositoryException extends BizException {
    public RepositoryException(String code, String msg) {
        super(code, msg);
    }

    public RepositoryException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}
