package com.hysaas.common.exception;

import lombok.Getter;

/** 业务异常，由 GlobalExceptionHandler 统一捕获 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
