package com.example.backend.repository;

import com.example.backend.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 角色数据访问接口。
 */
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /** 角色标识重名校验 */
    boolean existsByRoleKeyAndIsDeleted(String roleKey, Integer isDeleted);
}