package com.example.backend.repository;

import com.example.backend.entity.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 岗位数据访问接口。
 */
public interface SysPostRepository extends JpaRepository<SysPost, Long> {

    /** 岗位标识重名校验 */
    boolean existsByPostKeyAndIsDeleted(String postKey, Integer isDeleted);
}