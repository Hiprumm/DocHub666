package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户-角色关联实体，对应表 sys_user_role。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_role")
public class SysUserRole extends BaseRelationEntity {

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 角色ID */
    @Column(name = "role_id", nullable = false)
    private Long roleId;
}