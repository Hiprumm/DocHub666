package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.entity.SysRolePermission;
import com.example.backend.entity.SysUserDept;
import com.example.backend.entity.SysUserPermission;
import com.example.backend.entity.SysUserPost;
import com.example.backend.entity.SysUserRole;
import com.example.backend.repository.SysDeptRepository;
import com.example.backend.repository.SysPermissionRepository;
import com.example.backend.repository.SysPostRepository;
import com.example.backend.repository.SysRolePermissionRepository;
import com.example.backend.repository.SysRoleRepository;
import com.example.backend.repository.SysUserDeptRepository;
import com.example.backend.repository.SysUserPermissionRepository;
import com.example.backend.repository.SysUserPostRepository;
import com.example.backend.repository.SysUserRepository;
import com.example.backend.repository.SysUserRoleRepository;
import com.example.backend.security.PermissionAspect;
import com.example.backend.service.SysRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关联/分配业务实现。
 *
 * <p>铁律：删除旧关联后用 {@code saveAll} 批量写入，严禁循环单条 save；
 * 变更角色/权限后调用 {@link PermissionAspect#invalidateUser} 使权限缓存即时失效。</p>
 */
@Service
@RequiredArgsConstructor
public class SysRelationServiceImpl implements SysRelationService {

    private final SysUserDeptRepository userDeptRepository;
    private final SysUserPostRepository userPostRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysUserPermissionRepository userPermissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    private final SysUserRepository userRepository;
    private final SysDeptRepository deptRepository;
    private final SysPostRepository postRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    private final PermissionAspect permissionAspect;

    @Override
    @Transactional
    public void assignUserDepts(Long userId, List<Long> deptIds) {
        ensureUser(userId);
        List<Long> validIds = deptIds.stream().filter(id -> existsDept(id)).toList();
        userDeptRepository.deleteByUserId(userId);
        List<SysUserDept> rels = validIds.stream()
                .map(deptId -> {
                    SysUserDept rel = new SysUserDept();
                    rel.setUserId(userId);
                    rel.setDeptId(deptId);
                    return rel;
                }).toList();
        if (!rels.isEmpty()) {
            userDeptRepository.saveAll(rels);
        }
    }

    @Override
    @Transactional
    public void assignUserPosts(Long userId, List<Long> postIds) {
        ensureUser(userId);
        List<Long> validIds = postIds.stream().filter(id -> existsPost(id)).toList();
        userPostRepository.deleteByUserId(userId);
        List<SysUserPost> rels = validIds.stream()
                .map(postId -> {
                    SysUserPost rel = new SysUserPost();
                    rel.setUserId(userId);
                    rel.setPostId(postId);
                    return rel;
                }).toList();
        if (!rels.isEmpty()) {
            userPostRepository.saveAll(rels);
        }
    }

    @Override
    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        ensureUser(userId);
        List<Long> validIds = roleIds.stream().filter(id -> existsRole(id)).toList();
        userRoleRepository.deleteByUserId(userId);
        List<SysUserRole> rels = validIds.stream()
                .map(roleId -> {
                    SysUserRole rel = new SysUserRole();
                    rel.setUserId(userId);
                    rel.setRoleId(roleId);
                    return rel;
                }).toList();
        if (!rels.isEmpty()) {
            userRoleRepository.saveAll(rels);
        }
        // 角色变更影响用户派生权限，立即失效缓存
        permissionAspect.invalidateUser(userId);
    }

    @Override
    @Transactional
    public void assignUserPermissions(Long userId, List<Long> permissionIds) {
        ensureUser(userId);
        List<Long> validIds = permissionIds.stream().filter(id -> existsPermission(id)).toList();
        userPermissionRepository.deleteByUserId(userId);
        List<SysUserPermission> rels = validIds.stream()
                .map(permId -> {
                    SysUserPermission rel = new SysUserPermission();
                    rel.setUserId(userId);
                    rel.setPermissionId(permId);
                    return rel;
                }).toList();
        if (!rels.isEmpty()) {
            userPermissionRepository.saveAll(rels);
        }
        // 直挂权限变更，立即失效该用户缓存
        permissionAspect.invalidateUser(userId);
    }

    @Override
    @Transactional
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        if (!existsRole(roleId)) {
            throw new BusinessException("角色不存在或已被删除");
        }
        List<Long> validIds = permissionIds.stream().filter(id -> existsPermission(id)).toList();
        rolePermissionRepository.deleteByRoleId(roleId);
        List<SysRolePermission> rels = validIds.stream()
                .map(permId -> {
                    SysRolePermission rel = new SysRolePermission();
                    rel.setRoleId(roleId);
                    rel.setPermissionId(permId);
                    return rel;
                }).toList();
        if (!rels.isEmpty()) {
            rolePermissionRepository.saveAll(rels);
        }
        // 角色权限变更影响该角色下所有用户，逐个失效其权限缓存
        List<Long> affectedUsers = userRoleRepository.findByRoleId(roleId).stream()
                .map(SysUserRole::getUserId).distinct().collect(Collectors.toList());
        affectedUsers.forEach(permissionAspect::invalidateUser);
    }

    private void ensureUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在或已被删除");
        }
    }

    @Override
    public List<Long> listUserDepts(Long userId) {
        return userDeptRepository.findByUserId(userId).stream()
                .map(SysUserDept::getDeptId).toList();
    }

    @Override
    public List<Long> listUserPosts(Long userId) {
        return userPostRepository.findByUserId(userId).stream()
                .map(SysUserPost::getPostId).toList();
    }

    @Override
    public List<Long> listUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId).toList();
    }

    @Override
    public List<Long> listUserPermissions(Long userId) {
        return userPermissionRepository.findByUserId(userId).stream()
                .map(SysUserPermission::getPermissionId).toList();
    }

    @Override
    public List<Long> listRolePermissions(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(SysRolePermission::getPermissionId).toList();
    }

    private boolean existsDept(Long id) {
        return deptRepository.findById(id).map(d -> d.getIsDeleted() == 0).orElse(false);
    }

    private boolean existsPost(Long id) {
        return postRepository.findById(id).map(p -> p.getIsDeleted() == 0).orElse(false);
    }

    private boolean existsRole(Long id) {
        return roleRepository.findById(id).map(r -> r.getIsDeleted() == 0).orElse(false);
    }

    private boolean existsPermission(Long id) {
        return permissionRepository.findById(id).map(p -> p.getIsDeleted() == 0).orElse(false);
    }
}