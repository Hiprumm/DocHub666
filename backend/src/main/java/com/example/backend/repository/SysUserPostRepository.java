package com.example.backend.repository;

import com.example.backend.entity.SysUserPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 用户-岗位关联数据访问接口。
 */
public interface SysUserPostRepository extends JpaRepository<SysUserPost, Long> {

    /** 查询某用户的全部岗位关联 */
    List<SysUserPost> findByUserId(Long userId);

    /** 批量查询多个用户的全部岗位关联（一次 IN，供列表聚合避免循环单查） */
    List<SysUserPost> findByUserIdIn(Collection<Long> userIds);

    /** 删除某用户的全部岗位关联（重新分配前清旧） */
    void deleteByUserId(Long userId);

    /** 查询某岗位的全部用户关联（供岗位删除/停用前校验） */
    List<SysUserPost> findByPostId(Long postId);
}