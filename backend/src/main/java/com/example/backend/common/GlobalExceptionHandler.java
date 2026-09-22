package com.example.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：统一将异常转换为 {@link Result} 响应体。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；"));
        return Result.fail(HttpStatus.BAD_REQUEST.value(), msg);
    }

    /** 未登录 / 凭证失效：返回 401 */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.fail(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    /** 权限不足 / 越权：返回 403 */
    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handlePermissionDenied(PermissionDeniedException e) {
        return Result.fail(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

    /** 乐观锁版本冲突：第二道防线，兜底捕获底层 @Version 拦截，返回 409 */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleOptimisticLocking(ObjectOptimisticLockingFailureException e) {
        return Result.fail(HttpStatus.CONFLICT.value(), "数据已被他人修改，请刷新后重试");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception e) {
        // 未预期的内部异常：完整堆栈仅落服务端日志，接口只返回通用提示，避免向外部泄露 SQL/内部结构等敏感信息
        log.error("系统内部异常", e);
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后重试");
    }
}