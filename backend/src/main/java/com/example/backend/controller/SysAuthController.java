package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.SysAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * RBAC 授权绑定 REST 接口。
 */
@RestController
@RequestMapping("/sys/auth")
@RequiredArgsConstructor
public class SysAuthController {

    private final SysAuthService authService;

    /** 给用户分配角色：POST /sys/auth/user/{userId}/roles */
    @PostMapping("/user/{userId}/roles")
    public Result<Void> assignRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        authService.assignRolesToUser(userId, roleIds);
        return Result.ok();
    }

    /** 给用户直挂权限：POST /sys/auth/user/{userId}/permissions */
    @PostMapping("/user/{userId}/permissions")
    public Result<Void> assignPermissionsToUser(@PathVariable Long userId, @RequestBody List<Long> permissionIds) {
        authService.assignPermissionsToUser(userId, permissionIds);
        return Result.ok();
    }

    /** 给角色分配权限：POST /sys/auth/role/{roleId}/permissions */
    @PostMapping("/role/{roleId}/permissions")
    public Result<Void> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        authService.assignPermissionsToRole(roleId, permissionIds);
        return Result.ok();
    }

    /** 查询用户绑定的角色与直挂权限 */
    @GetMapping("/user/{userId}")
    public Result<Map<String, List<Long>>> getUserAuth(@PathVariable Long userId) {
        return Result.ok(Map.of(
                "roleIds", authService.listRoleIdsByUser(userId),
                "permissionIds", authService.listPermissionIdsByUser(userId)));
    }

    /** 查询角色绑定的权限 */
    @GetMapping("/role/{roleId}/permissionIds")
    public Result<List<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        return Result.ok(authService.listPermissionIdsByRole(roleId));
    }
}