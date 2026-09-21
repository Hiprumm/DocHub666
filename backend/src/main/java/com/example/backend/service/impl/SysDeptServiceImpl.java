package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.SysDeptDto;
import com.example.backend.entity.SysDept;
import com.example.backend.repository.SysDeptRepository;
import com.example.backend.service.SysDeptService;
import com.example.backend.vo.SysDeptVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 部门业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptRepository deptRepository;

    @Override
    @Transactional
    public SysDeptVo save(SysDeptDto dto) {
        SysDept dept;
        if (dto.getId() == null) {
            dept = new SysDept();
            buildAncestors(null, dto.getParentId(), dept);
        } else {
            dept = deptRepository.findById(dto.getId())
                    .filter(d -> Objects.equals(d.getIsDeleted(), 0))
                    .orElseThrow(() -> new BusinessException("部门不存在或已被删除"));
            // 乐观锁第一道防线：更新时前置版本冲突校验
            if (!Objects.equals(dept.getVersion(), dto.getVersion())) {
                throw new BusinessException(409, "数据已被他人修改，请刷新后重试");
            }
            // 若父级变更则重算祖级链
            if (dto.getParentId() != null && !dto.getParentId().equals(dept.getParentId())) {
                buildAncestors(dept.getId(), dto.getParentId(), dept);
            }
        }
        dept.setDeptName(dto.getDeptName().trim());
        dept.setOrderNum(dto.getOrderNum() == null ? 0 : dto.getOrderNum());
        dept.setLeaderUserId(dto.getLeaderUserId());
        dept.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        return toVo(deptRepository.save(dept));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysDept dept = deptRepository.findById(id)
                .filter(d -> Objects.equals(d.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("部门不存在或已被删除"));
        boolean hasChildren = deptRepository.findByParentIdAndIsDeletedOrderByOrderNumAsc(id, 0)
                .stream().anyMatch(c -> Objects.equals(c.getIsDeleted(), 0));
        if (hasChildren) {
            throw new BusinessException("该部门存在子部门，请先删除子部门");
        }
        dept.setIsDeleted(1);
        deptRepository.save(dept);
    }

    @Override
    public SysDeptVo getById(Long id) {
        return toVo(deptRepository.findById(id)
                .filter(d -> Objects.equals(d.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("部门不存在或已被删除")));
    }

    @Override
    public List<SysDeptVo> tree() {
        // 只拉取未删除部门，由数据库过滤，避免全表加载后在内存过滤
        List<SysDept> all = deptRepository.findAllByIsDeleted(0);
        Map<Long, SysDeptVo> voMap = new LinkedHashMap<>();
        for (SysDept d : all) {
            voMap.put(d.getId(), toVo(d));
        }
        List<SysDeptVo> roots = new ArrayList<>();
        for (SysDeptVo vo : voMap.values()) {
            Long parentId = Long.valueOf(vo.getParentId());
            if (parentId == 0L || !voMap.containsKey(parentId)) {
                roots.add(vo);
            }
        }
        return roots;
    }

    @Override
    public List<SysDeptVo> children(Long parentId) {
        return deptRepository.findByParentIdAndIsDeletedOrderByOrderNumAsc(parentId, 0)
                .stream().map(this::toVo).toList();
    }

    /** 计算并写入祖级链 */
    private void buildAncestors(Long selfId, Long parentId, SysDept dept) {
        if (parentId == null || parentId == 0L) {
            dept.setParentId(0L);
            dept.setAncestors("0");
            return;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException("父部门不能为自身");
        }
        SysDept parent = deptRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("父部门不存在"));
        dept.setParentId(parentId);
        dept.setAncestors(parent.getAncestors() + "," + parentId);
    }

    private SysDeptVo toVo(SysDept dept) {
        SysDeptVo vo = new SysDeptVo();
        vo.setId(String.valueOf(dept.getId()));
        vo.setParentId(String.valueOf(dept.getParentId()));
        vo.setAncestors(dept.getAncestors());
        vo.setDeptName(dept.getDeptName());
        vo.setOrderNum(dept.getOrderNum());
        vo.setLeaderUserId(dept.getLeaderUserId() == null ? null : String.valueOf(dept.getLeaderUserId()));
        vo.setStatus(dept.getStatus());
        vo.setCreateTime(dept.getCreateTime());
        return vo;
    }
}