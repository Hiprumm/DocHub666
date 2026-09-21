package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色-权限关联实体，对应表 sys_role_permission。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_role_permission")
public class SysRolePermission extends BaseRelationEntity {

    /** 角色ID */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /** 权限ID */
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
}