package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.SysUserCreateDto;
import com.example.backend.dto.SysUserUpdateDto;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;
import com.example.backend.entity.SysUser;
import com.example.backend.entity.SysUserDept;
import com.example.backend.entity.SysUserPost;
import com.example.backend.entity.SysUserRole;
import com.example.backend.repository.SysUserDeptRepository;
import com.example.backend.repository.SysUserPostRepository;
import com.example.backend.repository.SysUserRepository;
import com.example.backend.repository.SysUserRoleRepository;
import com.example.backend.security.AuthContext;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.SysUserService;
import com.example.backend.vo.SysUserVo;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 用户业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository userRepository;
    private final SysUserDeptRepository userDeptRepository;
    private final SysUserPostRepository userPostRepository;
    private final SysUserRoleRepository userRoleRepository;

    /** BCrypt 密码编码器（复用 SecurityConfig 单例 Bean） */
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
        SysUser saved = userRepository.save(user);

        // 创建入参若携带初始多关联则一并建立（多对多批量写入，禁循环单查）
        saveUserDepts(saved.getId(), dto.getDeptIds());
        saveUserPosts(saved.getId(), dto.getPostIds());
        saveUserRoles(saved.getId(), dto.getRoleIds());

        SysUserVo vo = toVo(saved);
        fillRelations(Map.of(saved.getId(), vo));
        return vo;
    }

    @Override
    @Transactional
    public SysUserVo update(SysUserUpdateDto dto) {
        SysUser user = getUserOrThrow(dto.getId());
        // 乐观锁第一道防线：前置版本冲突校验
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
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            auditLogService.record(AuthContext.getCurrentUserId(), AuthContext.getCurrentUsername(),
                    AuditAction.EDIT, "USER", user.getId(), OperationResult.SUCCESS, "修改密码");
        }
        SysUserVo vo = toVo(userRepository.save(user));
        fillRelations(Map.of(vo.getId() != null ? Long.valueOf(vo.getId()) : user.getId(), vo));
        return vo;
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
        SysUserVo vo = toVo(getUserOrThrow(id));
        fillRelations(Map.of(id, vo));
        return vo;
    }

    @Override
    public PageResult<SysUserVo> page(int pageNum, int pageSize, Long deptId, Long postId, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));

        // 岗位过滤：多对多存于 sys_user_post，先按岗位取用户ID集合，避免破坏主查询 Specification
        final List<Long> postFilterUserIds;
        if (postId != null) {
            List<Long> ids = userPostRepository.findByPostId(postId).stream()
                    .map(SysUserPost::getUserId).distinct().toList();
            if (ids.isEmpty()) {
                return PageResult.of(new PageImpl<>(List.of(), pageable, 0));
            }
            postFilterUserIds = ids;
        } else {
            postFilterUserIds = null;
        }

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
            if (postFilterUserIds != null) {
                predicates.add(root.get("id").in(postFilterUserIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysUser> page = userRepository.findAll(spec, pageable);
        List<SysUserVo> vos = page.getContent().stream().map(this::toVo).toList();
        Map<Long, SysUserVo> voMap = vos.stream()
                .collect(Collectors.toMap(v -> Long.valueOf(v.getId()), v -> v, (a, b) -> a));
        fillRelations(voMap);
        Page<SysUserVo> result = new PageImpl<>(vos, pageable, page.getTotalElements());
        return PageResult.of(result);
    }

    private boolean notDeleted(SysUser u) {
        return Objects.equals(u.getIsDeleted(), 0);
    }

    private SysUser getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(this::notDeleted)
                .orElseThrow(() -> new BusinessException("用户不存在或已被删除"));
    }

    /* ---------- 关联集合装载（批量，禁循环单查） ---------- */

    private void fillRelations(Map<Long, SysUserVo> voMap) {
        List<Long> userIds = new ArrayList<>(voMap.keySet());
        if (userIds.isEmpty()) {
            return;
        }
        // 三个关联表各一次 IN 批量查询，内存分组
        Map<Long, List<String>> deptMap = userDeptRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(SysUserDept::getUserId,
                        Collectors.mapping(r -> String.valueOf(r.getDeptId()), Collectors.toList())));
        Map<Long, List<String>> postMap = userPostRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(SysUserPost::getUserId,
                        Collectors.mapping(r -> String.valueOf(r.getPostId()), Collectors.toList())));
        Map<Long, List<String>> roleMap = userRoleRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(r -> String.valueOf(r.getRoleId()), Collectors.toList())));
        voMap.forEach((uid, vo) -> {
            vo.setDeptIds(deptMap.getOrDefault(uid, List.of()));
            vo.setPostIds(postMap.getOrDefault(uid, List.of()));
            vo.setRoleIds(roleMap.getOrDefault(uid, List.of()));
        });
    }

    private void saveUserDepts(Long userId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        userDeptRepository.saveAll(deptIds.stream()
                .map(id -> {
                    SysUserDept rel = new SysUserDept();
                    rel.setUserId(userId);
                    rel.setDeptId(id);
                    return rel;
                }).toList());
    }

    private void saveUserPosts(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        userPostRepository.saveAll(postIds.stream()
                .map(id -> {
                    SysUserPost rel = new SysUserPost();
                    rel.setUserId(userId);
                    rel.setPostId(id);
                    return rel;
                }).toList());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        userRoleRepository.saveAll(roleIds.stream()
                .map(id -> {
                    SysUserRole rel = new SysUserRole();
                    rel.setUserId(userId);
                    rel.setRoleId(id);
                    return rel;
                }).toList());
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
        vo.setVersion(String.valueOf(user.getVersion()));
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}