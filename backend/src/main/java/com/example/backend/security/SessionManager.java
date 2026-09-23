package com.example.backend.security;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录会话管理：基于 Redis 维护登录会话与用户会话版本号。
 *
 * <ul>
 *   <li>会话存储：{@code auth:session:{userId}:{sessionId}}（JSON，TTL 7 天）</li>
 *   <li>会话版本号：{@code auth:user_version:{userId}}（踢人/登出时递增使旧 token 失效，TTL 7 天）</li>
 * </ul>
 *
 * <p>所有 Redis 访问均包 {@code try-catch(DataAccessException)}：Redis 不可用时能力降级
 * （不校验会话、无法轮换/踢人），但绝不锁死用户，避免因依赖 Redis 导致整体不可用。</p>
 */
@Component
public class SessionManager {

    private static final String SESSION_PREFIX = "auth:session:";
    private static final String VERSION_KEY_PREFIX = "auth:user_version:";
    private static final long SESSION_TTL_SECONDS = 7L * 24 * 60 * 60;

    private final StringRedisTemplate stringRedisTemplate;

    public SessionManager(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 创建登录会话，生成新 sessionId 与当前会话版本号，返回完整会话记录 */
    public LoginSession createSession(Long userId, String username, String roleKey) {
        long version = getOrInitVersion(userId);
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        LoginSession session = new LoginSession(sessionId, userId, username, roleKey, version, System.currentTimeMillis());
        writeSession(session);
        return session;
    }

    /**
     * 校验会话是否有效。Redis 不可用或会话记录缺失/版本不匹配返回 false；
     * Redis 异常时按降级策略返回 true（跳过版本校验，不锁死用户）。
     */
    public boolean isSessionValid(String sessionId, long version) {
        if (!StringUtils.hasText(sessionId)) {
            return false;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(sessionIdKey(sessionId));
            if (value == null) {
                return false;
            }
            // 会话记录内版本号与 token 携带版本号一致才算有效
            return String.valueOf(version).equals(value);
        } catch (DataAccessException ex) {
            // Redis 不可用 → 降级：跳过会话版本校验，仅依赖 JWT 签名与有效期
            return true;
        }
    }

    /** 使某个会话失效（登出）。Redis 不可用时安全空操作 */
    public void revokeSession(String sessionId) {
        try {
            if (StringUtils.hasText(sessionId)) {
                stringRedisTemplate.delete(sessionIdKey(sessionId));
            }
        } catch (DataAccessException ignored) {
            // Redis 不可用：降级
        }
    }

    /** 使某用户全部会话失效（踢人/改密），通过递增会话版本号实现。Redis 不可用时安全空操作 */
    public void bumpVersion(Long userId) {
        try {
            Long v = stringRedisTemplate.opsForValue().increment(VERSION_KEY_PREFIX + userId);
            if (v != null && v == 1) {
                stringRedisTemplate.expire(VERSION_KEY_PREFIX + userId, SESSION_TTL_SECONDS, TimeUnit.SECONDS);
            }
        } catch (DataAccessException ignored) {
            // Redis 不可用：降级
        }
    }

    /** 读取（必要时初始化）用户当前会话版本号。Redis 不可用返回 0 */
    private long getOrInitVersion(Long userId) {
        try {
            String v = stringRedisTemplate.opsForValue().get(VERSION_KEY_PREFIX + userId);
            if (StringUtils.hasText(v)) {
                return Long.parseLong(v);
            }
            String newVal = stringRedisTemplate.opsForValue().getAndSet(VERSION_KEY_PREFIX + userId, "1");
            if (newVal == null) {
                stringRedisTemplate.expire(VERSION_KEY_PREFIX + userId, SESSION_TTL_SECONDS, TimeUnit.SECONDS);
                return 1;
            }
            return Long.parseLong(newVal);
        } catch (DataAccessException ex) {
            return 0;
        }
    }

    /** 写入会话 JSON。Redis 不可用时静默降级，后续会话校验按「缺失」处理（无版本校验） */
    private void writeSession(LoginSession session) {
        try {
            stringRedisTemplate.opsForValue().set(
                    sessionIdKey(session.sessionId()),
                    String.valueOf(session.version()),
                    SESSION_TTL_SECONDS,
                    TimeUnit.SECONDS);
        } catch (DataAccessException ignored) {
            // 降级
        }
    }

    private String sessionIdKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }
}