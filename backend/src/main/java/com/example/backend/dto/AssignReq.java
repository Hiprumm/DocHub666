package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 关联分配请求 DTO：批量绑定一组目标ID（部门/岗位/角色/权限）。
 */
@Getter
@Setter
public class AssignReq {

    /** 待绑定的目标ID列表（如 deptIds / postIds / roleIds / permissionIds） */
    @NotNull(message = "关联ID列表不能为空")
    private List<Long> ids;
}