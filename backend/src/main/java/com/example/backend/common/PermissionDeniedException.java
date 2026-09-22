package com.example.backend.common;

/**
 * 权限不足 / 越权异常（HTTP 403）。
 *
 * <p>由 {@code @RequirePermission} 权限切面在用户已登录但不具备所需权限点时抛出，
 * {@link GlobalExceptionHandler} 统一转换为 403 响应。供 M2 文档模块复用。</p>
 */
public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}