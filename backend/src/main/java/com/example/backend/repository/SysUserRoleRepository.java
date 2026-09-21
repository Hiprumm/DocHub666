package com.example.backend.repository;

import com.example.backend.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 用户-角色关联数据访问接口。
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    /** 查询某用户绑定的全部角色关联 */
    List<SysUserRole> findByUserId(Long userId);

    /** 查询某用户的指定角色关联（去重用） */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /** 删除某用户的全部角色关联 */
    void deleteByUserId(Long userId);
}