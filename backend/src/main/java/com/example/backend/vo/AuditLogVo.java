package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作审计日志视图对象。脱敏：仅返回留痕展示字段。
 */
@Getter
@Setter
public class AuditLogVo implements Serializable {

    private String id;
    private String actorId;
    private String actorName;
    private String action;
    private String targetType;
    private String targetId;
    private String result;
    private String detail;
    private LocalDateTime createTime;
}