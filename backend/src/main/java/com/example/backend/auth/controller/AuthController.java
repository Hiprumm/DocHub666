package com.example.backend.auth.controller;

import com.example.backend.auth.dto.CaptchaResp;
import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;
import com.example.backend.auth.dto.RefreshReq;
import com.example.backend.auth.dto.RefreshResp;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.Result;
import com.example.backend.security.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证接口。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * 登录：POST /auth/login。
     *
     * <p>业务失败（密码错误/账号不存在/停用）由 {@link com.example.backend.common.BusinessException}
     * 交给 {@link com.example.backend.common.GlobalExceptionHandler} 统一兜底返回。</p>
     */
    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return Result.ok(authService.login(req));
    }

    /**
     * 图形验证码：GET /auth/captcha。验证码未启用时返回空。
     */
    @GetMapping("/captcha")
    public Result<CaptchaResp> captcha() {
        CaptchaService.CaptchaAnswer answer = captchaService.generate();
        if (answer == null) {
            return Result.ok(new CaptchaResp(null, null));
        }
        return Result.ok(new CaptchaResp(answer.id(), answer.imageBase64()));
    }

    /**
     * 刷新令牌：POST /auth/refresh。凭 Refresh Token 换新一对令牌（Refresh 轮换）。
     */
    @PostMapping("/refresh")
    public Result<RefreshResp> refresh(@Valid @RequestBody RefreshReq req) {
        return Result.ok(authService.refresh(req));
    }

    /**
     * 登出当前会话：POST /auth/logout。使当前 Access 所对应会话的 Refresh Token 失效。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    /**
     * 退出全部登录端：POST /auth/logout-all。使当前用户所有会话失效。
     */
    @PostMapping("/logout-all")
    public Result<Void> logoutAll() {
        authService.logoutAll();
        return Result.ok();
    }
}