package com.example.backend.service.impl;

import com.example.backend.entity.AuditAction;
import com.example.backend.entity.AuditLog;
import com.example.backend.entity.OperationResult;
import com.example.backend.repository.AuditLogRepository;
import com.example.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作审计日志实现：异步写入 {@code audit_log} 表，日志写入失败不影响业务主流程（静默降级）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void record(Long actorId, String actorName, AuditAction action,
                       String targetType, Long targetId, OperationResult result, String detail) {
        try {
            AuditLog log = new AuditLog();
            log.setActorId(actorId);
            log.setActorName(actorName);
            log.setAction(action.name());
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setResult(result.name());
            log.setDetail(detail);
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // 审计为旁路能力：异步写入失败不应阻塞或影响主业务，仅记录告警日志
            log.warn("写入操作审计日志失败：actorId={} action={} detail={}，原因：{}",
                    actorId, action, detail, ex.getMessage());
        }
    }
}