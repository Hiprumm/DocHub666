package com.example.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * RSA 公钥响应 DTO（供前端 Web Crypto 加密登录密码）。
 */
@Getter
@Setter
@AllArgsConstructor
public class PublicKeyResp {

    /** 公钥 SPKI Base64；未启用 RSA 加密时为 null，前端回落明文 */
    private String publicKey;
}