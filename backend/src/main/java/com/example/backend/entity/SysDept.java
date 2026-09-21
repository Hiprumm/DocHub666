package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门实体，对应表 sys_dept。
 *
 * <p>树形结构采用「parentId + ancestors 扁平设计」，禁止自关联实体对象。</p>
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dept")
public class SysDept extends BaseEntity {

    /** 父部门ID，0 表示根节点 */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /** 祖级树链路径（如 0,100,101） */
    @Column(name = "ancestors", nullable = false, length = 500)
    private String ancestors = "0";

    /** 部门名称 */
    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    /** 同级显示排序号 */
    @Column(name = "order_num", nullable = false)
    private Integer orderNum = 0;

    /** 部门负责人用户ID */
    @Column(name = "leader_user_id")
    private Long leaderUserId;

    /** 部门状态：0-正常 1-停用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
}