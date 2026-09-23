package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.AuditLogQueryDto;
import com.example.backend.security.RequirePermission;
import com.example.backend.service.AuditLogService;
import com.example.backend.vo.AuditLogVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作审计日志 REST 接口（系统管理-操作日志）。
 */
@RestController
@RequestMapping("/sys/audit-log")
@RequiredArgsConstructor
public class SysAuditLogController {

    private final AuditLogService auditLogService;

    /** 分页查询操作日志 */
    @GetMapping("/page")
    @RequirePermission("system:log:query")
    public Result<PageResult<AuditLogVo>> page(@Valid AuditLogQueryDto query) {
        return Result.ok(auditLogService.page(query));
    }
}