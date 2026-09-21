package com.example.backend.repository;

import com.example.backend.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 角色-权限关联数据访问接口。
 */
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    /** 查询某角色绑定的全部权限关联 */
    List<SysRolePermission> findByRoleId(Long roleId);

    /** 查询某角色绑定的全部权限ID */
    List<SysRolePermission> findByPermissionId(Long permissionId);

    /** 去重校验 */
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /** 删除某角色的全部权限关联 */
    void deleteByRoleId(Long roleId);
}