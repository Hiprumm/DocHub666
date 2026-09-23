package com.example.backend.service;

import java.util.List;

/**
 * 关联/分配业务接口：维护用户与部门/岗位/角色/权限，以及角色与权限的多对多关系。
 *
 * <p>每次变更后需使受影响的用户权限缓存失效（{@code auth:perm:{userId}}），
 * 保证权限、角色变更即时生效。</p>
 */
public interface SysRelationService {

    /** 重设用户的部门集合（多对多） */
    void assignUserDepts(Long userId, List<Long> deptIds);

    /** 重设用户的岗位集合（多对多） */
    void assignUserPosts(Long userId, List<Long> postIds);

    /** 重设用户的角色集合（多对多），并失效该用户权限缓存 */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /** 重设用户的直挂权限集合（例外授权），并失效该用户权限缓存 */
    void assignUserPermissions(Long userId, List<Long> permissionIds);

    /** 重设角色的权限集合，并失效该角色下所有用户的权限缓存 */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    /* ---------- 只读查询（供编辑回显） ---------- */

    List<Long> listUserDepts(Long userId);

    List<Long> listUserPosts(Long userId);

    List<Long> listUserRoles(Long userId);

    List<Long> listUserPermissions(Long userId);

    List<Long> listRolePermissions(Long roleId);
}