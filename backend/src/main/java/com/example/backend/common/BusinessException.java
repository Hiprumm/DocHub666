package com.example.backend.common;

import lombok.Getter;

/**
 * 业务异常，用于在 Service 层抛出、由全局异常处理器统一转换为响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}