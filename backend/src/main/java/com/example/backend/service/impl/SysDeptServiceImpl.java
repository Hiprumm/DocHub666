package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CacheAsideService;
import com.example.backend.dto.SysDeptDto;
import com.example.backend.entity.SysDept;
import com.example.backend.repository.SysDeptRepository;
import com.example.backend.service.SysDeptService;
import com.example.backend.vo.SysDeptVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 部门业务实现。
 *
 * <p><b>缓存策略</b>：部门树为「读多写少」低频变更数据，走 Cache-Aside 缓存
 * （{@link CacheAsideService}）。读路径 {@link #tree()} 命中缓存直接返回，
 * 未命中全表加载建树后回填；写路径 {@link #save()} / {@link #delete()} 成功后
 * 删除缓存，保证数据一致。Redis 不可用时由 CacheAsideService 透明降级为每次直查库。</p>
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    /** 部门树缓存 key 前缀（与 auth:* 区分），实际 key 形如 biz:dept:tree */
    private static final String TREE_CACHE_KEY = "biz:dept:tree";
    /** 部门树缓存 TTL：树结构变更低频，设 30 分钟；写操作通过 delete 精确失效兜底 */
    private static final Duration TREE_CACHE_TTL = Duration.ofMinutes(30);

    private final SysDeptRepository deptRepository;
    private final CacheAsideService cacheAsideService;

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
        SysDeptVo vo = toVo(deptRepository.save(dept));
        cacheAsideService.invalidate(TREE_CACHE_KEY);
        return vo;
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
        cacheAsideService.invalidate(TREE_CACHE_KEY);
    }

    @Override
    public SysDeptVo getById(Long id) {
        return toVo(deptRepository.findById(id)
                .filter(d -> Objects.equals(d.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("部门不存在或已被删除")));
    }

    @Override
    public List<SysDeptVo> tree() {
        // Cache-Aside：命中缓存直接返回；未命中回源建树并回填
        return cacheAsideService.getList(TREE_CACHE_KEY, SysDeptVo.class, this::loadTree, TREE_CACHE_TTL);
    }

    /** 回源：只拉取未删除部门，由数据库过滤，构建根节点列表 */
    private List<SysDeptVo> loadTree() {
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
        vo.setVersion(String.valueOf(dept.getVersion()));
        vo.setCreateTime(dept.getCreateTime());
        return vo;
    }
}