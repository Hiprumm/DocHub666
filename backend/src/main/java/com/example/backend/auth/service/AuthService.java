package com.example.backend.auth.service;

import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;

/**
 * 身份认证业务接口。
 */
public interface AuthService {

    /**
     * 账号密码登录：BCrypt 校验 + JWT 签发。
     *
     * @return 登录成功响应（含 token）
     */
    LoginResp login(LoginReq req);
}