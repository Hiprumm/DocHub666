package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象（脱敏，禁止返回 password 与逻辑删除字段）。
 */
@Getter
@Setter
public class SysUserVo implements Serializable {

    private String id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String deptId;

    /** 多对多：用户所属部门ID集合（列表聚合展示） */
    private List<String> deptIds;

    /** 多对多：用户所属岗位ID集合 */
    private List<String> postIds;

    /** 多对多：用户角色ID集合 */
    private List<String> roleIds;

    private Integer status;
    private String version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}