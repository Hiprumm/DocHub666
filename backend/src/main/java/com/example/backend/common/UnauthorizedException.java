package com.example.backend.common;

/**
 * 未登录 / 登录凭证失效异常（HTTP 401）。
 *
 * <p>由鉴权切面在访问受保护资源但当前无有效用户身份时抛出，
 * {@link GlobalExceptionHandler} 统一转换为 401 响应。</p>
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}