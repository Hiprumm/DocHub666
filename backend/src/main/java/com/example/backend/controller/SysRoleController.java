package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.SysRoleDto;
import com.example.backend.service.SysRoleService;
import com.example.backend.vo.SysRoleVo;
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

/**
 * 角色管理 REST 接口。
 */
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @PostMapping
    public Result<SysRoleVo> create(@Valid @RequestBody SysRoleDto dto) {
        return Result.ok(roleService.save(dto));
    }

    @PutMapping
    public Result<SysRoleVo> update(@Valid @RequestBody SysRoleDto dto) {
        return Result.ok(roleService.save(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<SysRoleVo> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageResult<SysRoleVo>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(roleService.page(pageNum, pageSize));
    }
}