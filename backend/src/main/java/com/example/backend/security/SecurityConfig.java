package com.example.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Spring Security 配置（OpenAPI 风格 Lambda DSL）。
 *
 * <p>T1-4 收紧鉴权：接入 {@link JwtAuthFilter}（Bearer token 解析填充 AuthContext），
 * 仅放行登录接口 {@code /auth/login}，其余端点一律要求登录（authenticated()）。
 * 资源级权限靠 {@code @RequirePermission} 切面 + AuthContext 实现，Security 层只保证「已登录」。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /** BCrypt 密码编码器（登录校验与后续注册/改密共用） */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 无状态 JWT，关闭 CSRF 与会话
                .csrf(AbstractHttpConfigurer::disable)
                // 在 UsernamePasswordAuthenticationFilter 之前挂载 JWT 解析过滤器，填充 AuthContext
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 免认证白名单：登录、令牌刷新、RSA 公钥、图形验证码
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/public-key", "/auth/captcha").permitAll()
                        // 其余端点均要求登录（资源级权限由 @RequirePermission 切面细粒度控制）
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
}