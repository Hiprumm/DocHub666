package com.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 操作审计日志分页查询 DTO。
 */
@Getter
@Setter
public class AuditLogQueryDto {

    @Min(value = 1, message = "页码最小为 1")
    private int pageNum = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private int pageSize = 10;

    /** 操作人账号（模糊） */
    @Size(max = 50, message = "操作人最长 50 个字符")
    private String actorName;

    /** 动作类型（AuditAction 枚举词） */
    @Size(max = 50, message = "动作类型最长 50 个字符")
    private String action;

    /** 操作结果（OperationResult 枚举词） */
    @Size(max = 20, message = "结果最长 20 个字符")
    private String result;

    /** 开始时间（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime beginTime;

    /** 结束时间（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}