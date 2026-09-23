package com.example.backend.security;

import com.example.backend.common.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 密码加密传输工具：用私钥解密前端用公钥加密后的登录密码。
 *
 * <p>算法 {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding}（2048 位），私钥由环境变量
 * {@code APP_RSA_PRIVATE_KEY} 注入（PEM 的 PKCS#8 Base64 体）。公钥通过
 * {@code /auth/public-key} 接口下发，供前端 Web Crypto `crypto.subtle` 加密。
 * 私钥未配置/无效时能力降级为空（不抛出，避免拖垮启动），前端回落明文。
 */
@Component
public class RsaUtil {

    private static final String PADDING = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String ALGORITHM = "RSA";

    private final String privateKeyBase64;
    private PrivateKey privateKey;
    private String publicKeyBase64;

    public RsaUtil(@Value("${app.security.rsa.private-key:}") String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(privateKeyBase64)) {
            return;
        }
        try {
            byte[] der = Base64.getDecoder().decode(privateKeyBase64.trim());
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
            this.publicKeyBase64 = Base64.getEncoder().encodeToString(publicSpki());
        } catch (Exception ex) {
            // 私钥无效：禁用 RSA，前端回落明文，避免生产启动失败
            this.privateKey = null;
            this.publicKeyBase64 = "";
        }
    }

    /** 由私钥派生公钥 SPKI 字节（用于下发与前端 importKey）。 */
    private byte[] publicSpki() throws Exception {
        if (privateKey instanceof java.security.interfaces.RSAPrivateCrtKey crt) {
            java.security.spec.RSAPublicKeySpec pubSpec = new java.security.spec.RSAPublicKeySpec(
                    crt.getModulus(), crt.getPublicExponent());
            return KeyFactory.getInstance(ALGORITHM).generatePublic(pubSpec).getEncoded();
        }
        throw new IllegalStateException("私钥不是 RSAPrivateCrtKey，无法派生公钥");
    }

    /** RSA 是否已启用（私钥配置有效）。 */
    public boolean isEnabled() {
        return privateKey != null;
    }

    /** 返回下发给前端的公钥 Base64；未启用时返回空串，前端明文回退。 */
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    /** 用私钥解密前端密文（Base64）；失败抛"密码格式异常"。 */
    public String decrypt(String cipherBase64) {
        if (!isEnabled() || !StringUtils.hasText(cipherBase64)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "密码格式异常");
        }
        try {
            byte[] cipher = Base64.getDecoder().decode(cipherBase64.trim());
            Cipher c = Cipher.getInstance(PADDING);
            c.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(c.doFinal(cipher), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "密码格式异常");
        }
    }
}