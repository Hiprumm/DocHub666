package com.example.backend.service;

import com.example.backend.common.PageResult;
import com.example.backend.dto.SysRoleDto;
import com.example.backend.vo.SysRoleVo;

/**
 * 角色业务接口。
 */
public interface SysRoleService {

    SysRoleVo save(SysRoleDto dto);

    void delete(Long id);

    SysRoleVo getById(Long id);

    PageResult<SysRoleVo> page(int pageNum, int pageSize);
}