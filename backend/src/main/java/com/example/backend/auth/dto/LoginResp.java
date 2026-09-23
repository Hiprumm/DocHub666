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

    /** 短效访问令牌（Access Token，默认 2h） */
    private String accessToken;

    /** 长效刷新令牌（Refresh Token，默认 7d，用于换新 access） */
    private String refreshToken;

    /** Access Token 有效期（秒） */
    private long expiresIn;

    /** 用户ID（Long） */
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色标识（如 admin/viewer/editor，未绑定角色为 null） */
    private String roleKey;
}