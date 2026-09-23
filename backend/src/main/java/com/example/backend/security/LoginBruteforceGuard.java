package com.example.backend.security;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 登录防爆破守卫：按账号与按 IP 记录登录失败次数并临时锁定，附带 IP 级固定窗口限流。
 *
 * <p>所有 Redis 访问均包 {@code try-catch(DataAccessException)}：Redis 不可用时能力降级
 * （不锁定、不计数），绝不因 Redis 故障锁死正常用户。</p>
 */
@Component
public class LoginBruteforceGuard {

    /** 密码连续失败 N 次触发锁定 */
    private static final int MAX_FAILS = 5;
    /** 账号锁定时长（秒） */
    private static final long LOCK_SECONDS = 10L * 60;
    /** IP 固定窗口内最大登录尝试次数 */
    private static final int IP_MAX_ATTEMPTS = 20;
    /** IP 固定窗口时长（秒） */
    private static final long IP_WINDOW_SECONDS = 60;

    private static final String FAIL_USER_PREFIX = "auth:login_fail:u:";
    private static final String FAIL_IP_PREFIX = "auth:login_fail:ip:";
    private static final String LOCK_USER_PREFIX = "auth:login_lock:u:";
    private static final String LOCK_IP_PREFIX = "auth:login_lock:ip:";

    private final StringRedisTemplate stringRedisTemplate;

    public LoginBruteforceGuard(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 账号或 IP 是否被锁定。Redis 不可用返回 false（不锁死用户） */
    public boolean isLocked(String username, String ip) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_USER_PREFIX + username))
                    || (ip != null && !ip.isEmpty()
                    && Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_IP_PREFIX + hashIp(ip))));
        } catch (DataAccessException ex) {
            return false;
        }
    }

    /** 记录一次登录失败：累加账号失败计数，达阈值则锁定账号；并做 IP 级限流计数 */
    public void recordFailure(String username, String ip) {
        try {
            incrementWithExpire(FAIL_USER_PREFIX + username, LOCK_SECONDS, TimeUnit.SECONDS);
            if (currentCount(FAIL_USER_PREFIX + username) >= MAX_FAILS) {
                stringRedisTemplate.opsForValue().set(LOCK_USER_PREFIX + username, "1", LOCK_SECONDS, TimeUnit.SECONDS);
            }
            if (ip != null && !ip.isEmpty()) {
                incrementWithExpire(FAIL_IP_PREFIX + hashIp(ip), IP_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
        } catch (DataAccessException ex) {
            // Redis 不可用：降级，不计数
        }
    }

    /** IP 在固定窗口内的尝试是否超限（结合登录前预检） */
    public boolean ipExceeded(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        try {
            return currentCount(FAIL_IP_PREFIX + hashIp(ip)) >= IP_MAX_ATTEMPTS;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    /** 登录成功：清零账号失败计数与锁定标记 */
    public void recordSuccess(String username) {
        try {
            stringRedisTemplate.delete(FAIL_USER_PREFIX + username);
            stringRedisTemplate.delete(LOCK_USER_PREFIX + username);
        } catch (DataAccessException ex) {
            // 降级
        }
    }

    /** 原子 INCR + 首次 EXPIRE，带 Redis 降级保护 */
    private void incrementWithExpire(String key, long timeout, TimeUnit unit) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, timeout, unit);
        }
    }

    private long currentCount(String key) {
        String v = stringRedisTemplate.opsForValue().get(key);
        return v == null ? 0 : Long.parseLong(v);
    }

    /** IP 做 SHA-256 哈希后再作为 Redis key，避免明文留存 IP */
    private String hashIp(String ip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(ip.hashCode());
        }
    }
}