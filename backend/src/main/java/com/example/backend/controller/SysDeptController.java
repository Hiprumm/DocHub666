package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.SysDeptDto;
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
    public Result<SysDeptVo> create(@Valid @RequestBody SysDeptDto dto) {
        return Result.ok(deptService.save(dto));
    }

    @PutMapping
    public Result<SysDeptVo> update(@Valid @RequestBody SysDeptDto dto) {
        return Result.ok(deptService.save(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<SysDeptVo> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @GetMapping("/tree")
    public Result<List<SysDeptVo>> tree() {
        return Result.ok(deptService.tree());
    }

    @GetMapping("/children")
    public Result<List<SysDeptVo>> children(@RequestParam Long parentId) {
        return Result.ok(deptService.children(parentId));
    }
}