package com.example.backend.service;

import com.example.backend.common.PageResult;
import com.example.backend.dto.AuditLogQueryDto;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;
import com.example.backend.vo.AuditLogVo;

/**
 * 操作审计日志服务：统一 `record(...)` 写入钩子，供 M1/M2 全项目复用。
 *
 * <p>写入为异步（{@code @Async}），不阻塞业务主流程；日志含主体/动作/对象/结果/时间。</p>
 */
public interface AuditLogService {

    /** 记录一条审计日志 */
    void record(Long actorId, String actorName, AuditAction action,
                String targetType, Long targetId, OperationResult result, String detail);

    /** 分页查询审计日志（供系统管理-操作日志页，同步读取） */
    PageResult<AuditLogVo> page(AuditLogQueryDto query);
}