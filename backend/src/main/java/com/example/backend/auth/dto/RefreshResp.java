package com.example.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 刷新令牌（Refresh）响应出参 DTO：返回新签发的一对令牌（Refresh 已轮换，旧值失效）。
 */
@Getter
@Setter
public class RefreshResp implements Serializable {

    /** 新的短效访问令牌 */
    private String accessToken;

    /** 新的长效刷新令牌（轮换后的新值） */
    private String refreshToken;

    /** Access Token 有效期（秒） */
    private long expiresIn;
}