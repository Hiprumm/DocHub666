package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.SysPermissionDto;
import com.example.backend.security.RequirePermission;
import com.example.backend.service.SysPermissionService;
import com.example.backend.vo.SysPermissionVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理 REST 接口。
 */
@RestController
@RequestMapping("/sys/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @PostMapping
    @RequirePermission("system:perm:add")
    public Result<SysPermissionVo> create(@Valid @RequestBody SysPermissionDto dto) {
        return Result.ok(permissionService.save(dto));
    }

    @PutMapping
    @RequirePermission("system:perm:update")
    public Result<SysPermissionVo> update(@Valid @RequestBody SysPermissionDto dto) {
        return Result.ok(permissionService.save(dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:perm:delete")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:perm:query")
    public Result<SysPermissionVo> getById(@PathVariable Long id) {
        return Result.ok(permissionService.getById(id));
    }

    @GetMapping("/tree")
    @RequirePermission("system:perm:query")
    public Result<List<SysPermissionVo>> tree() {
        return Result.ok(permissionService.tree());
    }

    @GetMapping("/children")
    @RequirePermission("system:perm:query")
    public Result<List<SysPermissionVo>> children(@RequestParam Long parentId) {
        return Result.ok(permissionService.children(parentId));
    }
}