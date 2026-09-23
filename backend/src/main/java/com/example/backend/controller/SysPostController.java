package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.SysPostDto;
import com.example.backend.security.RequirePermission;
import com.example.backend.service.SysPostService;
import com.example.backend.vo.SysPostVo;
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
 * 岗位管理 REST 接口。
 */
@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService postService;

    @PostMapping
    @RequirePermission("system:post:add")
    public Result<SysPostVo> create(@Valid @RequestBody SysPostDto dto) {
        return Result.ok(postService.save(dto));
    }

    @PutMapping
    @RequirePermission("system:post:update")
    public Result<SysPostVo> update(@Valid @RequestBody SysPostDto dto) {
        return Result.ok(postService.save(dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:post:delete")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @RequirePermission("system:post:query")
    public Result<SysPostVo> getById(@PathVariable Long id) {
        return Result.ok(postService.getById(id));
    }

    @GetMapping("/list")
    @RequirePermission("system:post:query")
    public Result<PageResult<SysPostVo>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(postService.page(pageNum, pageSize));
    }
}