package com.example.backend.service;

import com.example.backend.dto.SysDeptDto;
import com.example.backend.vo.SysDeptVo;

import java.util.List;

/**
 * 部门业务接口。
 */
public interface SysDeptService {

    /** 创建或更新部门（id 为空则创建，否则更新） */
    SysDeptVo save(SysDeptDto dto);

    /** 逻辑删除部门（存在子部门时禁止删除） */
    void delete(Long id);

    /** 按 ID 查询 */
    SysDeptVo getById(Long id);

    /** 查询部门树（全量未删除） */
    List<SysDeptVo> tree();

    /** 查询某父部门下的子部门 */
    List<SysDeptVo> children(Long parentId);
}