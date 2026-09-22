package com.example.backend.service;

import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;

/**
 * 操作审计日志服务：统一 `record(...)` 写入钩子，供 M1/M2 全项目复用。
 *
 * <p>写入为异步（{@code @Async}），不阻塞业务主流程；日志含主体/动作/对象/结果/时间。</p>
 */
public interface AuditLogService {

    /** 记录一条审计日志 */
    void record(Long actorId, String actorName, AuditAction action,
                String targetType, Long targetId, OperationResult result, String detail);
}