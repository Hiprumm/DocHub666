package com.example.backend.repository;

import com.example.backend.entity.SysUserDept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 用户-部门关联数据访问接口。
 */
public interface SysUserDeptRepository extends JpaRepository<SysUserDept, Long> {

    /** 查询某用户的全部部门关联 */
    List<SysUserDept> findByUserId(Long userId);

    /** 批量查询多个用户的全部部门关联（一次 IN，供列表聚合避免循环单查） */
    List<SysUserDept> findByUserIdIn(Collection<Long> userIds);

    /** 删除某用户的全部部门关联（重新分配前清旧） */
    void deleteByUserId(Long userId);

    /** 查询某部门的全部用户关联（供部门删除/停用前校验） */
    List<SysUserDept> findByDeptId(Long deptId);
}