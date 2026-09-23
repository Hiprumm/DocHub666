package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体，对应表 sys_user。
 *
 * <p>deptId 为单值逻辑外键（一个用户仅属于一个部门）。</p>
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** 密码哈希（BCrypt） */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /** 真实姓名 */
    @Column(name = "real_name", length = 50)
    private String realName;

    /** 工作邮箱 */
    @Column(name = "email", length = 100)
    private String email;

    /** 手机号 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 主部门ID（逻辑外键 sys_dept.id，可空；完整多部门见 sys_user_dept） */
    @Column(name = "dept_id")
    private Long deptId;

    /** 账号状态：0-正常 1-停用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
}