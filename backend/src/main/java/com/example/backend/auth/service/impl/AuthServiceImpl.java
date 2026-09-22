package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.LoginReq;
import com.example.backend.auth.dto.LoginResp;
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
import com.example.backend.security.JwtUtil;
import com.example.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 身份认证业务实现：BCrypt 密码校验 + JWT 签发。
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

    @Override
    public LoginResp login(LoginReq req) {
        // 一次 IN 单行查询未删除用户；账号不存在与密码错误统一提示，防撞库
        SysUser user = userRepository.findByUsernameAndIsDeleted(req.getUsername(), 0)
                .orElse(null);
        if (user == null) {
            recordLogin(null, req.getUsername(), OperationResult.FORBIDDEN, "账号不存在");
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }

        // 停用校验：status=1 视为停用（403 语义）
        if (user.getStatus() != null && user.getStatus() == 1) {
            recordLogin(user.getId(), user.getUsername(), OperationResult.FORBIDDEN, "账号已停用");
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "账号已停用");
        }

        // BCrypt 密码校验；注意：新数据必须存 BCrypt 哈希，命中明文旧数据将校验失败
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            recordLogin(user.getId(), user.getUsername(), OperationResult.FORBIDDEN, "密码错误");
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }

        String roleKey = resolveRoleKey(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleKey);

        recordLogin(user.getId(), user.getUsername(), OperationResult.SUCCESS, "登录成功");

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setRoleKey(roleKey);
        return resp;
    }

    /** 记录登录审计（异步，不阻塞登录主流程；账号不存在时仅保留尝试账号名） */
    private void recordLogin(Long actorId, String actorName, OperationResult result, String detail) {
        auditLogService.record(actorId, actorName, AuditAction.LOGIN, "USER", actorId, result,
                StringUtils.hasText(detail) ? detail : result.name());
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