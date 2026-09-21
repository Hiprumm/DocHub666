package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限视图对象。
 */
@Getter
@Setter
public class SysPermissionVo implements Serializable {

    private String id;
    private String parentId;
    private String ancestors;
    private String permName;
    private String permKey;
    private Integer permType;
    private String path;
    private String method;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}