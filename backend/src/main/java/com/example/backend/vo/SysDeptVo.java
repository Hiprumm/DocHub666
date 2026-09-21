package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门视图对象。
 */
@Getter
@Setter
public class SysDeptVo implements Serializable {

    private String id;
    private String parentId;
    private String ancestors;
    private String deptName;
    private Integer orderNum;
    private String leaderUserId;
    private Integer status;
    private LocalDateTime createTime;
}