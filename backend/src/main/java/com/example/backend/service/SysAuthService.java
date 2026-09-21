package com.example.backend.service;

import java.util.List;

/**
 * RBAC 授权绑定业务接口（用户-角色、用户-权限、角色-权限三张关联表）。
 */
public interface SysAuthService {

    /** 给用户分配角色（全量覆盖） */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /** 给用户直挂权限（全量覆盖） */
    void assignPermissionsToUser(Long userId, List<Long> permissionIds);

    /** 给角色分配权限（全量覆盖） */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /** 查询用户绑定的角色ID列表 */
    List<Long> listRoleIdsByUser(Long userId);

    /** 查询用户直挂的权限ID列表 */
    List<Long> listPermissionIdsByUser(Long userId);

    /** 查询角色绑定的权限ID列表 */
    List<Long> listPermissionIdsByRole(Long roleId);
}