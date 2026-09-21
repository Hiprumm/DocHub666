package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色视图对象。
 */
@Getter
@Setter
public class SysRoleVo implements Serializable {

    private String id;
    private String roleName;
    private String roleKey;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}