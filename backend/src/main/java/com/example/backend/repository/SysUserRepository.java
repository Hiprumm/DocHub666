package com.example.backend.repository;

import com.example.backend.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 用户数据访问接口。
 * 多条件动态检索统一使用 {@link JpaSpecificationExecutor}，避免 if/else 拼接 SQL。
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>,
        JpaSpecificationExecutor<SysUser> {

    /** 按用户名查询未删除用户 */
    Optional<SysUser> findByUsernameAndIsDeleted(String username, Integer isDeleted);

    /** 用户名重名校验 */
    boolean existsByUsernameAndIsDeleted(String username, Integer isDeleted);
}