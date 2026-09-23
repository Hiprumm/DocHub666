package com.example.backend.security;

/**
 * 当前请求用户上下文（ThreadLocal）。
 *
 * <p>由 {@link JwtAuthFilter} 在每个请求进入过滤器链时填充（仅解析一次 JWT，不查库），
 * 业务层与权限切面通过静态方法获取当前登录用户信息；请求结束由过滤器 finally 调用
 * {@link #clear()} 清理，避免 ThreadLocal 内存泄漏与跨请求串数据。</p>
 */
public final class AuthContext {

    private AuthContext() {
    }

    /** 当前请求用户信息（不可变记录） */
    public record UserInfo(Long userId, String username, String roleKey, String sessionId, Long version) {

        /** 兼容旧调用：不携带会话信息时 version/sessionId 为 null */
        public UserInfo(Long userId, String username, String roleKey) {
            this(userId, username, roleKey, null, null);
        }
    }

    /** ThreadLocal 持有当前请求的用户标识信息 */
    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    /** 填充当前请求用户信息（含会话标识与版本） */
    public static void set(Long userId, String username, String roleKey, String sessionId, Long version) {
        HOLDER.set(new UserInfo(userId, username, roleKey, sessionId, version));
    }

    /** 当前登录用户ID（未登录返回 null） */
    public static Long getCurrentUserId() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.userId();
    }

    /** 当前登录用户名（未登录返回 null） */
    public static String getCurrentUsername() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.username();
    }

    /** 当前登录用户角色标识（未登录或无角色返回 null） */
    public static String getCurrentRoleKey() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.roleKey();
    }

    /** 当前登录用户的会话标识（未携带返回 null） */
    public static String getCurrentSessionId() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.sessionId();
    }

    /** 当前登录用户的会话版本号（未携带返回 null） */
    public static Long getCurrentVersion() {
        UserInfo info = HOLDER.get();
        return info == null ? null : info.version();
    }

    /** 当前是否已登录（存在有效 userId） */
    public static boolean isAuthenticated() {
        UserInfo info = HOLDER.get();
        return info != null && info.userId() != null;
    }

    /** 清理当前请求上下文（务必在请求结束调用，防泄漏） */
    public static void clear() {
        HOLDER.remove();
    }
}