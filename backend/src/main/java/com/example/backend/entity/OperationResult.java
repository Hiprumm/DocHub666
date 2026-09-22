package com.example.backend.entity;

/**
 * 操作结果枚举（对齐 GLOSSARY「OperationResult」）。
 *
 * <p>与 {@code audit_log.result} 列对应，统一供审计日志与自动化断言复用。</p>
 */
public enum OperationResult {

    /** 成功（200/201） */
    SUCCESS,

    /** 越权 / 权限不足（403） */
    FORBIDDEN,

    /** 请求错误（400） */
    BAD_REQUEST,

    /** 参数校验失败（422） */
    VALIDATION_ERROR
}