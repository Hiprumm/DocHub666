package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.SysDeptDto;
import com.example.backend.security.RequirePermission;
import com.example.backend.service.SysDeptService;
import com.example.backend.vo.SysDeptVo;
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
 * 部门管理 REST 接口。
 */
@RestController
@RequestMapping("/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @PostMapping
    @RequirePermission("system:dept:add")
    public Result<SysDeptVo> create(@Valid @RequestBody SysDeptDto dto) {
        return Result.ok(deptService.save(dto));
    }

    @PutMapping
    @RequirePermission("system:dept:update")
    public Result<SysDeptVo> update(@Valid @RequestBody SysDeptDto dto) {
        return Result.ok(deptService.save(dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:delete")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:dept:query")
    public Result<SysDeptVo> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @GetMapping("/tree")
    @RequirePermission("system:dept:query")
    public Result<List<SysDeptVo>> tree() {
        return Result.ok(deptService.tree());
    }

    @GetMapping("/children")
    @RequirePermission("system:dept:query")
    public Result<List<SysDeptVo>> children(@RequestParam Long parentId) {
        return Result.ok(deptService.children(parentId));
    }
}