package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户-权限关联实体，对应表 sys_user_permission。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_permission")
public class SysUserPermission extends BaseRelationEntity {

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 权限ID */
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
}