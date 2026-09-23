package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位实体，对应表 sys_post（复用 sys_role 列集风格）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_post")
public class SysPost extends BaseEntity {

    /** 岗位名称（展示用） */
    @Column(name = "post_name", nullable = false, length = 50)
    private String postName;

    /** 岗位标识（唯一，如 manager/member） */
    @Column(name = "post_key", nullable = false, unique = true, length = 50)
    private String postKey;

    /** 显示排序号 */
    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    /** 岗位状态：0-正常 1-停用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /** 岗位备注 */
    @Column(name = "remark", length = 255)
    private String remark;
}