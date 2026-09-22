package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.SysUserCreateDto;
import com.example.backend.dto.SysUserUpdateDto;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;
import com.example.backend.entity.SysUser;
import com.example.backend.repository.SysUserRepository;
import com.example.backend.security.AuthContext;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.SysUserService;
import com.example.backend.vo.SysUserVo;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository userRepository;

    /** BCrypt 密码编码器（复用 SecurityConfig 单例 Bean，create/update 加密存储） */
    private final BCryptPasswordEncoder passwordEncoder;

    /** 操作审计日志服务：改密等敏感操作异步留痕 */
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SysUserVo create(SysUserCreateDto dto) {
        if (userRepository.existsByUsernameAndIsDeleted(dto.getUsername(), 0)) {
            throw new BusinessException("登录账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername().trim());
        // BCrypt 加密后存储，绝不以明文落库
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        return toVo(userRepository.save(user));
    }

    @Override
    @Transactional
    public SysUserVo update(SysUserUpdateDto dto) {
        SysUser user = getUserOrThrow(dto.getId());
        // 乐观锁第一道防线：前置版本冲突校验（陈旧表单拦截）
        if (!Objects.equals(user.getVersion(), dto.getVersion())) {
            throw new BusinessException(409, "数据已被他人修改，请刷新后重试");
        }
        if (StringUtils.hasText(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getDeptId() != null) {
            user.setDeptId(dto.getDeptId());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        // 仅当传入新密码时重新 BCrypt 加密覆盖；未传则保持原有密文不变，避免二次加密或覆盖为空
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            // 改密为敏感操作，异步记录审计（含真实操作人，不泄露密码）
            auditLogService.record(AuthContext.getCurrentUserId(), AuthContext.getCurrentUsername(),
                    AuditAction.EDIT, "USER", user.getId(), OperationResult.SUCCESS, "修改密码");
        }
        return toVo(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = getUserOrThrow(id);
        user.setIsDeleted(1);
        userRepository.save(user);
    }

    @Override
    public SysUserVo getById(Long id) {
        return toVo(getUserOrThrow(id));
    }

    @Override
    public PageResult<SysUserVo> page(int pageNum, int pageSize, Long deptId, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));
        // 动态多条件查询：类型安全组合 isDeleted=0 + 可选 keyword/deptId，杜绝 if/else 拼接 SQL
        Specification<SysUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("realName")), pattern)
                ));
            }
            if (deptId != null) {
                predicates.add(cb.equal(root.get("deptId"), deptId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SysUser> page = userRepository.findAll(spec, pageable);
        return PageResult.of(page.map(this::toVo));
    }

    private boolean notDeleted(SysUser u) {
        return Objects.equals(u.getIsDeleted(), 0);
    }

    private SysUser getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(this::notDeleted)
                .orElseThrow(() -> new BusinessException("用户不存在或已被删除"));
    }

    private SysUserVo toVo(SysUser user) {
        SysUserVo vo = new SysUserVo();
        vo.setId(String.valueOf(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setDeptId(user.getDeptId() == null ? null : String.valueOf(user.getDeptId()));
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}