package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位创建/更新请求 DTO（复用 sys_role 风格）。
 */
@Getter
@Setter
public class SysPostDto {

    private Long id;

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 50, message = "岗位名称最长 50 个字符")
    private String postName;

    @NotBlank(message = "岗位标识不能为空")
    @Size(max = 50, message = "岗位标识最长 50 个字符")
    private String postKey;

    private Integer sort = 0;

    private Integer status = 0;

    @Size(max = 255, message = "备注最长 255 个字符")
    private String remark;

    /** 乐观锁版本号（更新时必传） */
    private Integer version;
}