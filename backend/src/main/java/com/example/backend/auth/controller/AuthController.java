package com.example.backend.auth.controller;

import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}