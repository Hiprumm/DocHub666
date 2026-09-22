package com.example.backend.entity;

import com.example.backend.common.SnowflakeIdWorker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体，对应表 audit_log（append-only 追加式留痕）。
 *
 * <p>区别于 {@link BaseEntity}：审计日志为不可变更的追加记录，故意不做乐观锁、逻辑删除，
 * 仅需主键、操作时间与 {@link AuditAction}/{@link OperationResult} 语义字段。</p>
 */
@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /** 日志ID（雪花算法） */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** 操作主体用户ID（未登录/系统行为可空） */
    @Column(name = "actor_id")
    private Long actorId;

    /** 操作主体账号名（冗余存，仅供留痕展示） */
    @Column(name = "actor_name", length = 50)
    private String actorName;

    /** 审计动作类型（AuditAction，与 {@code audit_action} 枚举词对齐） */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /** 操作对象类型（如 USER / ROLE / PERMISSION / DOCUMENT） */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /** 操作对象ID */
    @Column(name = "target_id")
    private Long targetId;

    /** 操作结果（OperationResult） */
    @Column(name = "result", nullable = false, length = 20)
    private String result;

    /** 附加元数据/说明 */
    @Column(name = "detail", length = 500)
    private String detail;

    /** 操作时间 */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = SnowflakeIdWorker.nextId();
        }
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}