package com.example.backend.security;

/**
 * 登录会话（不可变记录）。由 {@link SessionManager} 在登录/刷新时创建并持久化到 Redis。
 *
 * @param sessionId 会话唯一标识（对应 JWT 的 sid claim）
 * @param userId    用户ID
 * @param username  登录账号
 * @param roleKey   角色标识
 * @param version   会话版本号（踢人/改密时通过 bumpVersion 递增使旧 token 失效）
 * @param loginTime 登录时间戳（毫秒）
 */
public record LoginSession(String sessionId, Long userId, String username, String roleKey,
                           long version, long loginTime) {
}