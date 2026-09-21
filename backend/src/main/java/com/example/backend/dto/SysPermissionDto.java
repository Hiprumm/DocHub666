package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限创建/更新请求 DTO。
 */
@Getter
@Setter
public class SysPermissionDto {

    private Long id;

    private Long parentId = 0L;

    @Size(max = 500, message = "祖级树链过长")
    private String ancestors;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称最长 50 个字符")
    private String permName;

    @NotBlank(message = "权限标识不能为空")
    @Size(max = 100, message = "权限标识最长 100 个字符")
    private String permKey;

    @NotNull(message = "权限类型不能为空")
    private Integer permType = 1;

    @Size(max = 200, message = "路径最长 200 个字符")
    private String path;

    @Size(max = 10, message = "HTTP 方法最长 10 个字符")
    private String method;

    private Integer sort = 0;

    private Integer status = 0;

    /** 乐观锁版本号（更新时必传，创建时可为空） */
    private Integer version;
}