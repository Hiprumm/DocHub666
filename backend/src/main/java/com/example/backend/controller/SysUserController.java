package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.SysUserCreateDto;
import com.example.backend.dto.SysUserUpdateDto;
import com.example.backend.service.SysUserService;
import com.example.backend.vo.SysUserVo;
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
 * 用户管理 REST 接口。
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @PostMapping
    public Result<SysUserVo> create(@Valid @RequestBody SysUserCreateDto dto) {
        return Result.ok(userService.create(dto));
    }

    @PutMapping
    public Result<SysUserVo> update(@Valid @RequestBody SysUserUpdateDto dto) {
        return Result.ok(userService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<SysUserVo> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageResult<SysUserVo>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(pageNum, pageSize, deptId, keyword));
    }
}