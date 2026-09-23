package com.example.backend.service;

import com.example.backend.common.PageResult;
import com.example.backend.dto.SysUserCreateDto;
import com.example.backend.dto.SysUserUpdateDto;
import com.example.backend.vo.SysUserVo;

/**
 * 用户业务接口。
 */
public interface SysUserService {

    /** 创建用户 */
    SysUserVo create(SysUserCreateDto dto);

    /** 更新用户（不含密码） */
    SysUserVo update(SysUserUpdateDto dto);

    /** 按主键删除（逻辑删除） */
    void delete(Long id);

    /** 按主键查询 */
    SysUserVo getById(Long id);

    /** 分页查询（pageNum 从 1 开始），支持按主部门/岗位/关键词筛选 */
    PageResult<SysUserVo> page(int pageNum, int pageSize, Long deptId, Long postId, String keyword);
}