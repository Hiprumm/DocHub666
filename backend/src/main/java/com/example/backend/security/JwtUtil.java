package com.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责签发与解析无状态访问令牌（HS256）。
 *
 * <p>token 声明中携带 userId / username / roleKey，方便后续鉴权切面（T1-4）
 * 从 token 解析当前用户，避免每请求查库。</p>
 */
@Component
public class JwtUtil {

    /** token 有效期：24 小时 */
    private static final long EXPIRE_MILLIS = 24L * 60 * 60 * 1000;

    private static final int MIN_SECRET_LENGTH = 32;

    /** HS256 签名密钥（≥32 字节），由配置注入，避免硬编码在源码。生产环境经 ${JWT_SECRET} 读取 */
    private final String secret;

    private final SecretKey key;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("JWT 签名密钥 app.jwt.secret 缺失或长度不足 32 字节，请检查配置");
        }
        this.secret = secret;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 token：HS256 签名，claims 含 userId/username/roleKey，带 24h 过期时间 */
    public String generateToken(Long userId, String username, String roleKey) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRE_MILLIS);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim("roleKey", roleKey)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 token 得到 claims（签名或过期非法将抛异常，由调用方按需处理） */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /** 从 claims 取 userId */
    public Long getUserId(Claims claims) {
        Object v = claims.get("userId");
        return v == null ? null : Long.valueOf(String.valueOf(v));
    }

    /** 从 claims 取 username */
    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    /** 从 claims 取 roleKey */
    public String getRoleKey(Claims claims) {
        return claims.get("roleKey", String.class);
    }
}