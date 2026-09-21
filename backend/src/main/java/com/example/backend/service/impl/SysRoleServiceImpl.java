package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.SysRoleDto;
import com.example.backend.entity.SysRole;
import com.example.backend.repository.SysRoleRepository;
import com.example.backend.service.SysRoleService;
import com.example.backend.vo.SysRoleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 角色业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleRepository roleRepository;

    @Override
    @Transactional
    public SysRoleVo save(SysRoleDto dto) {
        SysRole role;
        if (dto.getId() == null) {
            if (roleRepository.existsByRoleKeyAndIsDeleted(dto.getRoleKey(), 0)) {
                throw new BusinessException("角色标识已存在");
            }
            role = new SysRole();
        } else {
            role = roleRepository.findById(dto.getId())
                    .filter(r -> Objects.equals(r.getIsDeleted(), 0))
                    .orElseThrow(() -> new BusinessException("角色不存在或已被删除"));
            // 乐观锁第一道防线：更新时前置版本冲突校验
            if (!Objects.equals(role.getVersion(), dto.getVersion())) {
                throw new BusinessException(409, "数据已被他人修改，请刷新后重试");
            }
            if (!dto.getRoleKey().equals(role.getRoleKey())
                    && roleRepository.existsByRoleKeyAndIsDeleted(dto.getRoleKey(), 0)) {
                throw new BusinessException("角色标识已存在");
            }
        }
        role.setRoleName(dto.getRoleName().trim());
        role.setRoleKey(dto.getRoleKey().trim());
        role.setSort(dto.getSort() == null ? 0 : dto.getSort());
        role.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        role.setRemark(dto.getRemark());
        return toVo(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysRole role = roleRepository.findById(id)
                .filter(r -> Objects.equals(r.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("角色不存在或已被删除"));
        role.setIsDeleted(1);
        roleRepository.save(role);
    }

    @Override
    public SysRoleVo getById(Long id) {
        return toVo(roleRepository.findById(id)
                .filter(r -> Objects.equals(r.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("角色不存在或已被删除")));
    }

    @Override
    public PageResult<SysRoleVo> page(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "sort"));
        Page<SysRoleVo> page = roleRepository.findAll(pageable)
                .map(this::toVo);
        return PageResult.of(page);
    }

    private SysRoleVo toVo(SysRole role) {
        SysRoleVo vo = new SysRoleVo();
        vo.setId(String.valueOf(role.getId()));
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setSort(role.getSort());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }
}