package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户-部门关联实体，对应表 sys_user_dept（多对多：一名用户可属多个部门）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_dept")
public class SysUserDept extends BaseRelationEntity {

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 部门ID */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
}