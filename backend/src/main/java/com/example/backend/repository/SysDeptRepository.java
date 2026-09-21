package com.example.backend.repository;

import com.example.backend.entity.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 部门数据访问接口。
 */
public interface SysDeptRepository extends JpaRepository<SysDept, Long> {

    /** 查询指定父部门下的子部门（按排序号升序） */
    List<SysDept> findByParentIdAndIsDeletedOrderByOrderNumAsc(Long parentId, Integer isDeleted);

    /** 查询全部未删除部门（供树形组装，替代全表加载后在内存过滤） */
    List<SysDept> findAllByIsDeleted(Integer isDeleted);

    /** 按部门名精确匹配（用于重名校验） */
    boolean existsByDeptNameAndIsDeleted(String deptName, Integer isDeleted);
}