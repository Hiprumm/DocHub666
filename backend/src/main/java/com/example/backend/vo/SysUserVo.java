package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}