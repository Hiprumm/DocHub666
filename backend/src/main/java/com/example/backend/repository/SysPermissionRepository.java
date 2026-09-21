package com.example.backend.repository;

import com.example.backend.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 权限数据访问接口。
 */
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    /** 权限标识重名校验 */
    boolean existsByPermKeyAndIsDeleted(String permKey, Integer isDeleted);

    /** 查询指定父权限下的子权限（按排序号） */
    List<SysPermission> findByParentIdAndIsDeletedOrderBySortAsc(Long parentId, Integer isDeleted);

    /** 查询全部未删除权限（供树形组装，替代全表加载后在内存过滤） */
    List<SysPermission> findAllByIsDeleted(Integer isDeleted);
}