package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色创建/更新请求 DTO。
 */
@Getter
@Setter
public class SysRoleDto {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长 50 个字符")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    @Size(max = 50, message = "角色标识最长 50 个字符")
    private String roleKey;

    private Integer sort = 0;

    private Integer status = 0;

    @Size(max = 255, message = "备注最长 255 个字符")
    private String remark;

    /** 乐观锁版本号（更新时必传，创建时可为空） */
    private Integer version;
}