package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色实体，对应表 sys_role。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称（展示用） */
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    /** 角色标识（唯一，如 admin/viewer/editor） */
    @Column(name = "role_key", nullable = false, unique = true, length = 50)
    private String roleKey;

    /** 显示排序号 */
    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    /** 角色状态：0-正常 1-停用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /** 角色备注 */
    @Column(name = "remark", length = 255)
    private String remark;
}