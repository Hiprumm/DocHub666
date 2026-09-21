package com.example.backend.repository;

import com.example.backend.entity.SysUserPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户-权限关联数据访问接口。
 */
public interface SysUserPermissionRepository extends JpaRepository<SysUserPermission, Long> {

    /** 查询某用户直挂的全部权限关联 */
    List<SysUserPermission> findByUserId(Long userId);

    /** 去重校验 */
    boolean existsByUserIdAndPermissionId(Long userId, Long permissionId);

    /** 删除某用户的全部直挂权限 */
    void deleteByUserId(Long userId);
}