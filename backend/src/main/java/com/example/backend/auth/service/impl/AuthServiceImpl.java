package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;
import com.example.backend.auth.dto.RefreshReq;
import com.example.backend.auth.dto.RefreshResp;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.BusinessException;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;
import com.example.backend.entity.SysRole;
import com.example.backend.entity.SysUser;
import com.example.backend.entity.SysUserRole;
import com.example.backend.repository.SysRoleRepository;
import com.example.backend.repository.SysUserRepository;
import com.example.backend.repository.SysUserRoleRepository;
import com.example.backend.security.AuthContext;
import com.example.backend.security.CaptchaService;
import com.example.backend.security.JwtUtil;
import com.example.backend.security.LoginBruteforceGuard;
import com.example.backend.security.LoginSession;
import com.example.backend.security.RsaUtil;
import com.example.backend.security.SessionManager;
import com.example.backend.service.AuditLogService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * 身份认证业务实现：BCrypt 密码校验 + 双 token（Access/Refresh）签发 + 会话管理 + 登录防爆破。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final SessionManager sessionManager;
    private final LoginBruteforceGuard bruteforceGuard;
    private final CaptchaService captchaService;
    private final RsaUtil rsaUtil;

    @Override
    public LoginResp login(LoginReq req) {
        String ip = clientIp();

        // 防爆破：账号/IP 被锁定则直接拒绝
        if (bruteforceGuard.isLocked(req.getUsername(), ip)) {
            recordLogin(null, req.getUsername(), OperationResult.FORBIDDEN, "账号已锁定");
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS.value(), "尝试次数过多，请稍后再试");
        }
        // IP 级固定窗口限流（登录接口高频尝试）
        if (bruteforceGuard.ipExceeded(ip)) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS.value(), "操作过于频繁，请稍后再试");
        }
        // 图形验证码（启用时校验，一次性核销）
        if (captchaService.isEnabled() && !captchaService.verify(req.getCaptchaId(), req.getCaptchaCode())) {
            recordLogin(null, req.getUsername(), OperationResult.FORBIDDEN, "验证码错误");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "验证码错误或已失效");
        }

        // RSA 密码解密：若启用了密码加密传输，则先解密再走 BCrypt 校验
        if (rsaUtil.isEnabled()) {
            req.setPassword(rsaUtil.decrypt(req.getPassword()));
        }

        // 一次 IN 单行查询未删除用户；账号不存在与密码错误统一提示，防撞库
        SysUser user = userRepository.findByUsernameAndIsDeleted(req.getUsername(), 0)
                .orElse(null);
        if (user == null) {
            bruteforceGuard.recordFailure(req.getUsername(), ip);
            recordLogin(null, req.getUsername(), OperationResult.FORBIDDEN, "账号不存在");
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }

        // 停用校验：status=1 视为停用（403 语义）
        if (user.getStatus() != null && user.getStatus() == 1) {
            bruteforceGuard.recordFailure(req.getUsername(), ip);
            recordLogin(user.getId(), user.getUsername(), OperationResult.FORBIDDEN, "账号已停用");
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "账号已停用");
        }

        // BCrypt 密码校验；注意：新数据必须存 BCrypt 哈希，命中明文旧数据将校验失败
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            bruteforceGuard.recordFailure(req.getUsername(), ip);
            recordLogin(user.getId(), user.getUsername(), OperationResult.FORBIDDEN, "密码错误");
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }

        // 登录成功：清零失败计数
        bruteforceGuard.recordSuccess(req.getUsername());

        String roleKey = resolveRoleKey(user.getId());
        // 创建登录会话并签发双 token
        LoginSession session = sessionManager.createSession(user.getId(), user.getUsername(), roleKey);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(),
                roleKey, session.sessionId(), session.version());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(),
                session.sessionId(), session.version());

        recordLogin(user.getId(), user.getUsername(), OperationResult.SUCCESS, "登录成功");

        LoginResp resp = new LoginResp();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(2L * 60 * 60); // Access 默认 2h（秒）
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setRoleKey(roleKey);
        return resp;
    }

    @Override
    public RefreshResp refresh(RefreshReq req) {
        Claims claims;
        try {
            claims = jwtUtil.parseToken(req.getRefreshToken());
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "刷新令牌无效或已过期");
        }
        // 仅接受 Refresh Token
        if (!jwtUtil.isRefresh(claims)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "刷新令牌无效或已过期");
        }
        Long userId = jwtUtil.getUserId(claims);
        String sessionId = jwtUtil.getSessionId(claims);
        long version = jwtUtil.getVersion(claims);

        // 会话已失效（登出/被踢/Redis 会话不匹配），禁止换新
        if (!sessionManager.isSessionValid(sessionId, version)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "会话已失效，请重新登录");
        }

        // 校验用户仍存在且未停用
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getIsDeleted() == 1
                || (user.getStatus() != null && user.getStatus() == 1)) {
            sessionManager.revokeSession(sessionId);
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "账号不可用，请重新登录");
        }

        String roleKey = resolveRoleKey(userId);
        // 轮换会话：作废旧会话，重建新会话（Refresh 换新后旧 Refresh 立即失效，防重放）
        sessionManager.revokeSession(sessionId);
        LoginSession newSession = sessionManager.createSession(userId, user.getUsername(), roleKey);
        String accessToken = jwtUtil.generateAccessToken(userId, user.getUsername(),
                roleKey, newSession.sessionId(), newSession.version());
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, user.getUsername(),
                newSession.sessionId(), newSession.version());

        RefreshResp resp = new RefreshResp();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(newRefreshToken);
        resp.setExpiresIn(2L * 60 * 60);
        return resp;
    }

    @Override
    public void logout() {
        String sessionId = AuthContext.getCurrentSessionId();
        if (StringUtils.hasText(sessionId)) {
            sessionManager.revokeSession(sessionId);
        }
    }

    @Override
    public void logoutAll() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId != null) {
            sessionManager.bumpVersion(userId);
        }
    }

    /** 记录登录审计（异步，不阻塞登录主流程；账号不存在时仅保留尝试账号名） */
    private void recordLogin(Long actorId, String actorName, OperationResult result, String detail) {
        auditLogService.record(actorId, actorName, AuditAction.LOGIN, "USER", actorId, result,
                StringUtils.hasText(detail) ? detail : result.name());
    }

    /** 从当前请求提取客户端 IP（多级代理取 X-Forwarded-For 首段）；无请求上下文返回空串 */
    private String clientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "";
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwarded)) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return "";
        }
    }

    /** 解析用户首个有效角色的角色标识（无角色返回 null；一次 IN 批量取值，避免循环单查） */
    private String resolveRoleKey(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return null;
        }
        return roleRepository.findAllById(roleIds).stream()
                .filter(r -> r.getIsDeleted() == 0 && r.getStatus() == 0)
                .sorted(Comparator.comparing(SysRole::getSort))
                .map(SysRole::getRoleKey)
                .findFirst()
                .orElse(null);
    }
}