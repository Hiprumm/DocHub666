package com.example.backend.entity;

import com.example.backend.common.SnowflakeIdWorker;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 实体审计基类（MappedSuperclass）。
 *
 * <p>统一承载雪花 ID 主键、逻辑删除与审计字段（创建/更新人+时间）。
 * 子类通过继承自动获得统一字段，无需重复声明。</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /** 主键（雪花算法 Long，序列化时转 String） */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** 逻辑删除标记：0-正常 1-已删除 */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    /** 创建人用户ID */
    @Column(name = "create_by")
    private Long createBy;

    /** 创建时间 */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /** 更新人用户ID */
    @Column(name = "update_by")
    private Long updateBy;

    /** 更新时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** 乐观锁版本号（JPA @Version）：更新时自动生成 WHERE id=? AND version=?，为 0 行则抛乐观锁冲突 */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = SnowflakeIdWorker.nextId();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.updateTime == null) {
            this.updateTime = now;
        }
        if (this.isDeleted == null) {
            this.isDeleted = 0;
        }
    }
}