package com.example.backend.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位视图对象。
 */
@Getter
@Setter
public class SysPostVo implements Serializable {

    private String id;
    private String postName;
    private String postKey;
    private Integer sort;
    private Integer status;
    private String remark;
    private String version;
    private LocalDateTime createTime;
}