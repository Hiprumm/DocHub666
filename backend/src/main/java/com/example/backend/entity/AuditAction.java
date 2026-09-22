package com.example.backend.entity;

/**
 * 审计动作类型枚举（对齐 GLOSSARY「AuditAction」）。
 *
 * <p>值约定与数据库 {@code audit_log.action} 一致，禁止同义替换；
 * 登录/访问/上传/编辑/权限变更等动作统一从此枚举取词，写入审计日志。</p>
 */
public enum AuditAction {

    /** 登录（成功/失败均留痕） */
    LOGIN,

    /** 访问 / 越权尝试 */
    ACCESS,

    /** 上传 */
    UPLOAD,

    /** 编辑 */
    EDIT,

    /** 版本回滚 */
    VERSION_RESTORE,

    /** 删除 */
    DELETE,

    /** 回收 */
    RECYCLE,

    /** AI 合成入库 */
    AI_CONFIRM,

    /** AI 合成归档 */
    AI_ARCHIVE,

    /** 权限变更（授权/撤权/角色/权限点变更） */
    PERMISSION_CHANGE
}