package com.example.backend.service;

import com.example.backend.dto.SysPermissionDto;
import com.example.backend.vo.SysPermissionVo;

import java.util.List;

/**
 * 权限业务接口。
 */
public interface SysPermissionService {

    SysPermissionVo save(SysPermissionDto dto);

    void delete(Long id);

    SysPermissionVo getById(Long id);

    List<SysPermissionVo> tree();

    List<SysPermissionVo> children(Long parentId);
}