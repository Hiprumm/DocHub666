package com.example.backend.service.impl;

import com.example.backend.common.PageResult;
import com.example.backend.dto.AuditLogQueryDto;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.AuditLog;
import com.example.backend.entity.OperationResult;
import com.example.backend.repository.AuditLogRepository;
import com.example.backend.service.AuditLogService;
import com.example.backend.vo.AuditLogVo;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public PageResult<AuditLogVo> page(AuditLogQueryDto query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPageNum() - 1, 0), query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        // 动态多条件筛选：操作人/动作/结果/时间范围，类型安全 Specification
        Specification<AuditLog> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query.getActorName())) {
                predicates.add(cb.like(root.get("actorName"), "%" + query.getActorName().trim() + "%"));
            }
            if (StringUtils.hasText(query.getAction())) {
                predicates.add(cb.equal(root.get("action"), query.getAction().trim()));
            }
            if (StringUtils.hasText(query.getResult())) {
                predicates.add(cb.equal(root.get("result"), query.getResult().trim()));
            }
            if (query.getBeginTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), query.getBeginTime()));
            }
            if (query.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), query.getEndTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<AuditLogVo> page = auditLogRepository.findAll(spec, pageable).map(this::toVo);
        return PageResult.of(page);
    }

    private AuditLogVo toVo(AuditLog log) {
        AuditLogVo vo = new AuditLogVo();
        vo.setId(String.valueOf(log.getId()));
        vo.setActorId(log.getActorId() == null ? null : String.valueOf(log.getActorId()));
        vo.setActorName(log.getActorName());
        vo.setAction(log.getAction());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId() == null ? null : String.valueOf(log.getTargetId()));
        vo.setResult(log.getResult());
        vo.setDetail(log.getDetail());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}