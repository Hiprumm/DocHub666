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
    public record UserInfo(Long userId, String username, String roleKey) {
    }

    /** ThreadLocal 持有当前请求的用户标识信息 */
    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    /** 填充当前请求用户信息 */
    public static void set(Long userId, String username, String roleKey) {
        HOLDER.set(new UserInfo(userId, username, roleKey));
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