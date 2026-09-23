package com.example.backend.auth.service;

import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;
import com.example.backend.auth.dto.RefreshReq;
import com.example.backend.auth.dto.RefreshResp;

/**
 * 身份认证业务接口。
 */
public interface AuthService {

    /**
     * 账号密码登录：BCrypt 校验 + 双 token（Access/Refresh）签发 + 会话创建。
     *
     * @return 登录成功响应（含 accessToken/refreshToken）
     */
    LoginResp login(LoginReq req);

    /**
     * 凭 Refresh Token 换新：校验签名/类型/会话有效性后轮换出新一对令牌，旧会话失效。
     *
     * @return 新签发的令牌对
     */
    RefreshResp refresh(RefreshReq req);

    /**
     * 登出当前会话：使当前请求所携带会话失效（Refresh Token 将无法继续换新）。
     */
    void logout();

    /**
     * 使当前用户全部会话失效（踢出所有登录端）。
     */
    void logoutAll();
}