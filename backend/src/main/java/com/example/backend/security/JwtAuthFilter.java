package com.example.backend.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 解析过滤器。
 *
 * <p>从 {@code Authorization: Bearer <token>} 请求头解析 token，成功后通过
 * {@link AuthContext#set} 填充当前用户信息（仅解析一次，不查库）；token 缺失或非法/过期
 * 则 AuthContext 留空，由后续 Security / {@code @RequirePermission} 切面按需判 401/403。
 * 无论是否成功，{@code finally} 中必须清理 ThreadLocal 防泄漏。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null) {
                try {
                    Claims claims = jwtUtil.parseToken(token);
                    AuthContext.set(jwtUtil.getUserId(claims), jwtUtil.getUsername(claims), jwtUtil.getRoleKey(claims));
                } catch (Exception ignored) {
                    // token 签名非法或已过期：不在此处抛出，AuthContext 留空，
                    // 由后续鉴权环节（Security 或权限切面）统一返回 401
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    /** 解析 Authorization 头的 Bearer token，无则返回 null */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}