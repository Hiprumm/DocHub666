package com.example.backend.auth.controller;

import com.example.backend.auth.dto.PublicKeyResp;
import com.example.backend.common.Result;
import com.example.backend.security.RsaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RSA 公钥下发接口（登录密码加密传输用）。
 *
 * <p>公开端点，无需鉴权；私钥未配置时返回 {@code publicKey=null}，前端回落明文登录。</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PublicKeyController {

    private final RsaUtil rsaUtil;

    /**
     * 获取 RSA 公钥：GET /auth/public-key。
     */
    @GetMapping("/public-key")
    public Result<PublicKeyResp> publicKey() {
        String pk = rsaUtil.getPublicKeyBase64();
        return Result.ok(new PublicKeyResp(
                pk == null || pk.isEmpty() ? null : pk));
    }
}