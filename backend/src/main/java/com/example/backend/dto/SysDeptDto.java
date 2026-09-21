package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门创建/更新请求 DTO。
 */
@Getter
@Setter
public class SysDeptDto {

    private Long id;

    private Long parentId = 0L;

    /** 祖级树链（新建子部门时由后端计算，前端可不传） */
    @Size(max = 500, message = "祖级树链过长")
    private String ancestors;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称最长 50 个字符")
    private String deptName;

    private Integer orderNum = 0;

    private Long leaderUserId;

    private Integer status = 0;

    /** 乐观锁版本号（更新时必传，创建时可为空） */
    private Integer version;
}