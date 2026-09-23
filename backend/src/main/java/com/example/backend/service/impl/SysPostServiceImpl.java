package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.SysPostDto;
import com.example.backend.entity.SysPost;
import com.example.backend.repository.SysPostRepository;
import com.example.backend.service.SysPostService;
import com.example.backend.vo.SysPostVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 岗位业务实现（复用 sys_role 全套模式）。
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl implements SysPostService {

    private final SysPostRepository postRepository;

    @Override
    @Transactional
    public SysPostVo save(SysPostDto dto) {
        SysPost post;
        if (dto.getId() == null) {
            if (postRepository.existsByPostKeyAndIsDeleted(dto.getPostKey(), 0)) {
                throw new BusinessException("岗位标识已存在");
            }
            post = new SysPost();
        } else {
            post = postRepository.findById(dto.getId())
                    .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                    .orElseThrow(() -> new BusinessException("岗位不存在或已被删除"));
            // 乐观锁第一道防线：更新时前置版本冲突校验
            if (!Objects.equals(post.getVersion(), dto.getVersion())) {
                throw new BusinessException(409, "数据已被他人修改，请刷新后重试");
            }
            if (!dto.getPostKey().equals(post.getPostKey())
                    && postRepository.existsByPostKeyAndIsDeleted(dto.getPostKey(), 0)) {
                throw new BusinessException("岗位标识已存在");
            }
        }
        post.setPostName(dto.getPostName().trim());
        post.setPostKey(dto.getPostKey().trim());
        post.setSort(dto.getSort() == null ? 0 : dto.getSort());
        post.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        post.setRemark(dto.getRemark());
        return toVo(postRepository.save(post));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysPost post = postRepository.findById(id)
                .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("岗位不存在或已被删除"));
        post.setIsDeleted(1);
        postRepository.save(post);
    }

    @Override
    public SysPostVo getById(Long id) {
        return toVo(postRepository.findById(id)
                .filter(p -> Objects.equals(p.getIsDeleted(), 0))
                .orElseThrow(() -> new BusinessException("岗位不存在或已被删除")));
    }

    @Override
    public PageResult<SysPostVo> page(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "sort"));
        Page<SysPostVo> page = postRepository.findAll(pageable).map(this::toVo);
        return PageResult.of(page);
    }

    private SysPostVo toVo(SysPost post) {
        SysPostVo vo = new SysPostVo();
        vo.setId(String.valueOf(post.getId()));
        vo.setPostName(post.getPostName());
        vo.setPostKey(post.getPostKey());
        vo.setSort(post.getSort());
        vo.setStatus(post.getStatus());
        vo.setRemark(post.getRemark());
        vo.setVersion(String.valueOf(post.getVersion()));
        vo.setCreateTime(post.getCreateTime());
        return vo;
    }
}