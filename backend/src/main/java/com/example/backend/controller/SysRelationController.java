package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.AssignReq;
import com.example.backend.security.RequirePermission;
import com.example.backend.service.SysRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 关联/分配管理 REST 接口：维护用户-部门/岗位/角色/权限、角色-权限的多对多关系。
 */
@RestController
@RequestMapping("/sys/relation")
@RequiredArgsConstructor
public class SysRelationController {

    private final SysRelationService relationService;

    @GetMapping("/user/{userId}/depts")
    @RequirePermission("system:user:query")
    public Result<List<Long>> listUserDepts(@PathVariable Long userId) {
        return Result.ok(relationService.listUserDepts(userId));
    }

    @GetMapping("/user/{userId}/posts")
    @RequirePermission("system:user:query")
    public Result<List<Long>> listUserPosts(@PathVariable Long userId) {
        return Result.ok(relationService.listUserPosts(userId));
    }

    @GetMapping("/user/{userId}/roles")
    @RequirePermission("system:user:query")
    public Result<List<Long>> listUserRoles(@PathVariable Long userId) {
        return Result.ok(relationService.listUserRoles(userId));
    }

    @GetMapping("/user/{userId}/permissions")
    @RequirePermission("system:user:query")
    public Result<List<Long>> listUserPermissions(@PathVariable Long userId) {
        return Result.ok(relationService.listUserPermissions(userId));
    }

    @GetMapping("/role/{roleId}/permissions")
    @RequirePermission("system:role:query")
    public Result<List<Long>> listRolePermissions(@PathVariable Long roleId) {
        return Result.ok(relationService.listRolePermissions(roleId));
    }

    @PutMapping("/user/{userId}/depts")
    @RequirePermission("system:user:update")
    public Result<Void> assignUserDepts(@PathVariable Long userId, @Valid @RequestBody AssignReq req) {
        relationService.assignUserDepts(userId, req.getIds());
        return Result.ok();
    }

    @PutMapping("/user/{userId}/posts")
    @RequirePermission("system:user:update")
    public Result<Void> assignUserPosts(@PathVariable Long userId, @Valid @RequestBody AssignReq req) {
        relationService.assignUserPosts(userId, req.getIds());
        return Result.ok();
    }

    @PutMapping("/user/{userId}/roles")
    @RequirePermission("system:user:update")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @Valid @RequestBody AssignReq req) {
        relationService.assignUserRoles(userId, req.getIds());
        return Result.ok();
    }

    @PutMapping("/user/{userId}/permissions")
    @RequirePermission("system:user:update")
    public Result<Void> assignUserPermissions(@PathVariable Long userId, @Valid @RequestBody AssignReq req) {
        relationService.assignUserPermissions(userId, req.getIds());
        return Result.ok();
    }

    @PutMapping("/role/{roleId}/permissions")
    @RequirePermission("system:role:update")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId, @Valid @RequestBody AssignReq req) {
        relationService.assignRolePermissions(roleId, req.getIds());
        return Result.ok();
    }
}