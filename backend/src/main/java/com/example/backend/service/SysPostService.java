package com.example.backend.service;

import com.example.backend.common.PageResult;
import com.example.backend.dto.SysPostDto;
import com.example.backend.vo.SysPostVo;

/**
 * 岗位业务接口。
 */
public interface SysPostService {

    SysPostVo save(SysPostDto dto);

    void delete(Long id);

    SysPostVo getById(Long id);

    PageResult<SysPostVo> page(int pageNum, int pageSize);
}