package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限实体，对应表 sys_permission。
 *
 * <p>菜单/接口级原子权限点，树形扁平设计。</p>
 */
@Getter
@Setter
@Entity
@Table(name = "sys_permission")
public class SysPermission extends BaseEntity {

    /** 父权限ID，0 表示根节点 */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /** 祖级树链路径 */
    @Column(name = "ancestors", nullable = false, length = 500)
    private String ancestors = "0";

    /** 权限名称 */
    @Column(name = "perm_name", nullable = false, length = 50)
    private String permName;

    /** 权限标识（唯一，如 doc:create） */
    @Column(name = "perm_key", nullable = false, unique = true, length = 100)
    private String permKey;

    /** 权限类型：1-目录 2-菜单 3-按钮/接口 */
    @Column(name = "perm_type", nullable = false)
    private Integer permType = 1;

    /** 前端路由或后端接口路径 */
    @Column(name = "path", length = 200)
    private String path;

    /** HTTP 方法（接口级权限用） */
    @Column(name = "method", length = 10)
    private String method;

    /** 显示排序号 */
    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    /** 权限状态：0-正常 1-停用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
}