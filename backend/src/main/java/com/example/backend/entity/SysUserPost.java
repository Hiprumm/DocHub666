package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户-岗位关联实体，对应表 sys_user_post（多对多：一名用户可兼任多岗位）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user_post")
public class SysUserPost extends BaseRelationEntity {

    /** 用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 岗位ID */
    @Column(name = "post_id", nullable = false)
    private Long postId;
}