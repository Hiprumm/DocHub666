package com.example.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 登录成功响应出参 DTO（脱敏，不含 password 等敏感字段）。
 */
@Getter
@Setter
public class LoginResp implements Serializable {

    /** JWT 访问令牌 */
    private String token;

    /** 用户ID（Long） */
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色标识（如 admin/viewer/editor，未绑定角色为 null） */
    private String roleKey;
}