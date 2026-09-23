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
 * JWT 工具类：负责签发与解析无状态令牌（HS256，双 token）。
 *
 * <p>签发短效 {@code Access Token}（携带 userId/username/roleKey/sessionId/version）与长效
 * {@code Refresh Token}（同样带会话标识，支持凭其换新与轮换）。cliams 不含密码等敏感字段，
 * roleKey 空值时置 null，避免 null 拼接。</p>
 */
@Component
public class JwtUtil {

    /** Access Token 有效期：2 小时（可经 app.jwt.access-minute 覆盖） */
    private static final long DEFAULT_ACCESS_MILLIS = 2L * 60 * 60 * 1000;

    /** Refresh Token 有效期：7 天（可经 app.jwt.refresh-days 覆盖） */
    private static final long DEFAULT_REFRESH_MILLIS = 7L * 24 * 60 * 60 * 1000;

    /** Token 类型 claim 名 */
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_SESSION = "sid";
    private static final String CLAIM_VERSION = "ver";

    private static final int MIN_SECRET_LENGTH = 32;

    /** HS256 签名密钥（≥32 字节），由配置注入，避免硬编码在源码。生产环境经 ${JWT_SECRET} 读取 */
    private final SecretKey key;

    private final long accessMillis;
    private final long refreshMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.access-minute:120}") long accessMinute,
                   @Value("${app.jwt.refresh-days:7}") long refreshDays) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("JWT 签名密钥 app.jwt.secret 缺失或长度不足 32 字节，请检查配置");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMillis = accessMinute > 0 ? accessMinute * 60 * 1000 : DEFAULT_ACCESS_MILLIS;
        this.refreshMillis = refreshDays > 0 ? refreshDays * 24 * 60 * 60 * 1000 : DEFAULT_REFRESH_MILLIS;
    }

    /** 签发 Access Token：HS256，claims 含 userId/username/roleKey/sid/ver，短效（默认 2h） */
    public String generateAccessToken(Long userId, String username, String roleKey, String sessionId, long version) {
        return buildToken(userId, username, roleKey, sessionId, version, TYPE_ACCESS, accessMillis);
    }

    /** 签发 Refresh Token：HS256，claims 含 userId/username/sid/ver，长效（默认 7d） */
    public String generateRefreshToken(Long userId, String username, String sessionId, long version) {
        return buildToken(userId, username, null, sessionId, version, TYPE_REFRESH, refreshMillis);
    }

    /** 通用 token 构建 */
    private String buildToken(Long userId, String username, String roleKey, String sessionId,
                              long version, String type, long expireMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMillis);
        var builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_SESSION, sessionId)
                .claim(CLAIM_VERSION, version)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);
        if (roleKey != null) {
            builder.claim("roleKey", roleKey);
        }
        return builder.compact();
    }

    /** 解析 token 得到 claims（签名或过期非法将抛异常，由调用方按需处理） */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /** 是否为 Access Token */
    public boolean isAccess(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    /** 是否为 Refresh Token */
    public boolean isRefresh(Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    /** 从 claims 取会话 id */
    public String getSessionId(Claims claims) {
        return claims.get(CLAIM_SESSION, String.class);
    }

    /** 从 claims 取会话版本号（缺省 0） */
    public long getVersion(Claims claims) {
        Object v = claims.get(CLAIM_VERSION);
        return v == null ? 0L : Long.parseLong(String.valueOf(v));
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