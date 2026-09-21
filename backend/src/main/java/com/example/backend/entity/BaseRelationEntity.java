package com.example.backend.entity;

import com.example.backend.common.SnowflakeIdWorker;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 关联表精简基类。
 *
 * <p>关联表仅保留主键、创建人、创建时间，无软删除与更新审计字段。</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseRelationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "create_by")
    private Long createBy;

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