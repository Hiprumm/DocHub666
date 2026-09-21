package com.example.backend.common;

import lombok.Getter;

import java.io.Serializable;

/**
 * 通用响应包装体。
 *
 * @param <T> 数据载荷类型
 */
@Getter
public class Result<T> implements Serializable {

    private final int code;
    private final String message;
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "ok", data);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}