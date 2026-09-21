package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.entity.SysPermission;
import com.example.backend.entity.SysRole;
import com.example.backend.entity.SysRolePermission;
import com.example.backend.entity.SysUserPermission;
import com.example.backend.entity.SysUserRole;
import com.example.backend.repository.SysPermissionRepository;
import com.example.backend.repository.SysRolePermissionRepository;
import com.example.backend.repository.SysRoleRepository;
import com.example.backend.repository.SysUserPermissionRepository;
import com.example.backend.repository.SysUserRepository;
import com.example.backend.repository.SysUserRoleRepository;
import com.example.backend.service.SysAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC 授权绑定业务实现。
 */
@Service
@RequiredArgsConstructor
public class SysAuthServiceImpl implements SysAuthService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysUserPermissionRepository userPermissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRepository.findById(userId)
                .filter(u -> u.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("用户不存在或已被删除"));
        Set<Long> ids = roleIds == null ? Set.of() : roleIds.stream().distinct().collect(Collectors.toSet());
        // 批量校验角色存在性（一次 IN 查询，避免循环单查）
        Map<Long, Long> existMap = roleRepository.findAllById(ids).stream()
                .filter(r -> r.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysRole::getId, SysRole::getId));
        if (existMap.size() != ids.size()) {
            throw new BusinessException("存在失效的角色ID");
        }
        userRoleRepository.deleteByUserId(userId);
        List<SysUserRole> rels = new ArrayList<>(ids.size());
        for (Long roleId : ids) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            rels.add(ur);
        }
        userRoleRepository.saveAll(rels); // 批量插入，替代循环单条 save
    }

    @Override
    @Transactional
    public void assignPermissionsToUser(Long userId, List<Long> permissionIds) {
        userRepository.findById(userId)
                .filter(u -> u.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("用户不存在或已被删除"));
        Set<Long> ids = permissionIds == null ? Set.of() : permissionIds.stream().distinct().collect(Collectors.toSet());
        Map<Long, Long> existMap = permissionRepository.findAllById(ids).stream()
                .filter(p -> p.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysPermission::getId, SysPermission::getId));
        if (existMap.size() != ids.size()) {
            throw new BusinessException("存在失效的权限ID");
        }
        userPermissionRepository.deleteByUserId(userId);
        List<SysUserPermission> rels = new ArrayList<>(ids.size());
        for (Long permId : ids) {
            SysUserPermission up = new SysUserPermission();
            up.setUserId(userId);
            up.setPermissionId(permId);
            rels.add(up);
        }
        userPermissionRepository.saveAll(rels);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        roleRepository.findById(roleId)
                .filter(r -> r.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("角色不存在或已被删除"));
        Set<Long> ids = permissionIds == null ? Set.of() : permissionIds.stream().distinct().collect(Collectors.toSet());
        Map<Long, Long> existMap = permissionRepository.findAllById(ids).stream()
                .filter(p -> p.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysPermission::getId, SysPermission::getId));
        if (existMap.size() != ids.size()) {
            throw new BusinessException("存在失效的权限ID");
        }
        rolePermissionRepository.deleteByRoleId(roleId);
        List<SysRolePermission> rels = new ArrayList<>(ids.size());
        for (Long permId : ids) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rels.add(rp);
        }
        rolePermissionRepository.saveAll(rels);
    }

    @Override
    public List<Long> listRoleIdsByUser(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId).toList();
    }

    @Override
    public List<Long> listPermissionIdsByUser(Long userId) {
        return userPermissionRepository.findByUserId(userId).stream()
                .map(SysUserPermission::getPermissionId).toList();
    }

    @Override
    public List<Long> listPermissionIdsByRole(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(SysRolePermission::getPermissionId).toList();
    }
}