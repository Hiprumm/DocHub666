package com.example.backend.repository;

import com.example.backend.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 用户-角色关联数据访问接口。
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    /** 查询某用户绑定的全部角色关联 */
    List<SysUserRole> findByUserId(Long userId);

    /** 查询绑定某角色的全部用户关联（供角色权限变更后批量失效相关用户缓存） */
    List<SysUserRole> findByRoleId(Long roleId);

    /** 批量查询多个用户的全部角色关联（一次 IN，供列表聚合避免循环单查） */
    List<SysUserRole> findByUserIdIn(Collection<Long> userIds);

    /** 查询某用户的指定角色关联（去重用） */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /** 删除某用户的全部角色关联 */
    void deleteByUserId(Long userId);
}