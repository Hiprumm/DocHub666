package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.SysPermissionDto;
import com.example.backend.entity.SysPermission;
import com.example.backend.repository.SysPermissionRepository;
import com.example.backend.service.SysPermissionService;
import com.example.backend.vo.SysPermissionVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 权限业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionRepository permissionRepository;

    @Override
    @Transactional
    public SysPermissionVo save(SysPermissionDto dto) {
        SysPermission perm;
        if (dto.getId() == null) {
            if (permissionRepository.existsByPermKeyAndIsDeleted(dto.getPermKey(), 0)) {
                throw new BusinessException("权限标识已存在");
            }
            perm = new SysPermission();
            buildAncestors(null, dto.getParentId(), perm);
        } else {
            perm = permissionRepository.findById(dto.getId())
                    .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                    .orElseThrow(() -> new BusinessException("权限不存在或已被删除"));
            // 乐观锁第一道防线：更新时前置版本冲突校验
            if (!Objects.equals(perm.getVersion(), dto.getVersion())) {
                throw new BusinessException(409, "数据已被他人修改，请刷新后重试");
            }
            if (!dto.getPermKey().equals(perm.getPermKey())
                    && permissionRepository.existsByPermKeyAndIsDeleted(dto.getPermKey(), 0)) {
                throw new BusinessException("权限标识已存在");
            }
            if (dto.getParentId() != null && !dto.getParentId().equals(perm.getParentId())) {
                buildAncestors(perm.getId(), dto.getParentId(), perm);
            }
        }
        perm.setPermName(dto.getPermName().trim());
        perm.setPermKey(dto.getPermKey().trim());
        perm.setPermType(dto.getPermType());
        perm.setPath(dto.getPath());
        perm.setMethod(dto.getMethod());
        perm.setSort(dto.getSort() == null ? 0 : dto.getSort());
        perm.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        return toVo(permissionRepository.save(perm));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysPermission perm = permissionRepository.findById(id)
                .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("权限不存在或已被删除"));
        boolean hasChildren = !permissionRepository.findByParentIdAndIsDeletedOrderBySortAsc(id, 0).isEmpty();
        if (hasChildren) {
            throw new BusinessException("该权限存在子权限，请先删除子权限");
        }
        perm.setIsDeleted(1);
        permissionRepository.save(perm);
    }

    @Override
    public SysPermissionVo getById(Long id) {
        return toVo(permissionRepository.findById(id)
                .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("权限不存在或已被删除")));
    }

    @Override
    public List<SysPermissionVo> tree() {
        // 只拉取未删除权限，由数据库过滤，避免全表加载后在内存过滤
        List<SysPermission> all = permissionRepository.findAllByIsDeleted(0);
        Map<Long, SysPermissionVo> voMap = new LinkedHashMap<>();
        for (SysPermission p : all) {
            voMap.put(p.getId(), toVo(p));
        }
        List<SysPermissionVo> roots = new ArrayList<>();
        for (SysPermissionVo vo : voMap.values()) {
            Long parentId = Long.valueOf(vo.getParentId());
            if (parentId == 0L || !voMap.containsKey(parentId)) {
                roots.add(vo);
            }
        }
        return roots;
    }

    @Override
    public List<SysPermissionVo> children(Long parentId) {
        return permissionRepository.findByParentIdAndIsDeletedOrderBySortAsc(parentId, 0)
                .stream().map(this::toVo).toList();
    }

    private void buildAncestors(Long selfId, Long parentId, SysPermission perm) {
        if (parentId == null || parentId == 0L) {
            perm.setParentId(0L);
            perm.setAncestors("0");
            return;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException("父权限不能为自身");
        }
        SysPermission parent = permissionRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("父权限不存在"));
        perm.setParentId(parentId);
        perm.setAncestors(parent.getAncestors() + "," + parentId);
    }

    private SysPermissionVo toVo(SysPermission perm) {
        SysPermissionVo vo = new SysPermissionVo();
        vo.setId(String.valueOf(perm.getId()));
        vo.setParentId(String.valueOf(perm.getParentId()));
        vo.setAncestors(perm.getAncestors());
        vo.setPermName(perm.getPermName());
        vo.setPermKey(perm.getPermKey());
        vo.setPermType(perm.getPermType());
        vo.setPath(perm.getPath());
        vo.setMethod(perm.getMethod());
        vo.setSort(perm.getSort());
        vo.setStatus(perm.getStatus());
        vo.setCreateTime(perm.getCreateTime());
        return vo;
    }
}