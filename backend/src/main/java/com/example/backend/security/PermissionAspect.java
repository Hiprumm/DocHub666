package com.example.backend.security;

import com.example.backend.common.PermissionDeniedException;
import com.example.backend.common.UnauthorizedException;
import com.example.backend.entity.AuditAction;
import com.example.backend.entity.OperationResult;
import com.example.backend.entity.SysPermission;
import com.example.backend.entity.SysRolePermission;
import com.example.backend.entity.SysUserPermission;
import com.example.backend.entity.SysUserRole;
import com.example.backend.repository.SysPermissionRepository;
import com.example.backend.repository.SysRolePermissionRepository;
import com.example.backend.repository.SysUserPermissionRepository;
import com.example.backend.repository.SysUserRoleRepository;
import com.example.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限校验切面：在标有 {@code @RequirePermission} 的方法执行前，
 * 校验当前登录用户（来自 {@link AuthContext}）是否拥有对应权限点（permKey）。
 *
 * <p>业务链路：JwtAuthFilter 已把 userId 放入 {@link AuthContext} → 此处取 userId
 * 批量查询其「角色派生 + 直挂」权限集合 → 不含所需 permKey 则判 403 / 未登录判 401。</p>
 *
 * <p><b>缓存策略</b>：用户权限集合写入 Redis（key 形如 {@code auth:perm:{userId}}，
 * value 为逗号分隔的 permKey 字符串），相比原内存缓存可跨应用实例共享；上层「授权 / 撤权 /
 * 角色变更」时调用 {@link #invalidateUser(Long)} 删除对应 key，实现权限变更即时生效。</p>
 *
 * <p><b>无 Redis 兜底</b>：Redis 为可选能力。Lettuce 连接工厂为懒连模式，本地未启动 Redis
 * 不会导致应用启动失败；所有 Redis 读写均以 try-catch 包裹，连接失败 / 命令异常时降级为
 * 「每次直接查库、不写缓存」，保证权限判断始终正确（仅性能略降，功能不受影响）。</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    /** 权限集合缓存 TTL（秒）：取 30 分钟。因上层授权/撤权可走 {@link #invalidateUser(Long)}
     *  精确失效缓存，故可放宽 TTL 降低高频请求的查库压力，同时保留过期自动刷新作为兜底。 */
    private static final long CACHE_TTL_SECONDS = TimeUnit.MINUTES.toSeconds(30);

    /** Redis key 前缀，实际 key 形如 auth:perm:{userId} */
    private static final String CACHE_KEY_PREFIX = "auth:perm:";

    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserPermissionRepository userPermissionRepository;
    private final SysPermissionRepository permissionRepository;

    /** Redis 客户端：value 为逗号分隔的 permKey 字符串，直接用 String 序列化即可 */
    private final StringRedisTemplate stringRedisTemplate;

    /** 操作审计日志服务：越权访问时异步记录 ACCESS/FORBIDDEN */
    private final AuditLogService auditLogService;

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录或登录已过期，请重新登录");
        }
        if (!loadPermissionKeys(userId).contains(requirePermission.value())) {
            auditLogService.record(userId, AuthContext.getCurrentUsername(), AuditAction.ACCESS,
                    "PERMISSION", null, OperationResult.FORBIDDEN,
                    "越权访问：" + requirePermission.value());
            throw new PermissionDeniedException("无权限执行该操作：" + requirePermission.value());
        }
    }

    /**
     * 加载用户拥有的全部权限点集合（角色派生 + 直挂）。
     *
     * <p>优先读 Redis 缓存（命中直接返回）；未命中则走批量查询（直挂一次取、角色一次 IN、
     * 角色权限一次 IN、权限实体一次 IN，内存合并去重），并回写 Redis 设 TTL。
     * Redis 不可用时退化到「每次直查库、不写缓存」。</p>
     */
    private Set<String> loadPermissionKeys(Long userId) {
        Set<String> cached = readFromCache(userId);
        if (cached != null) {
            return cached;
        }

        // 1) 用户直挂的权限ID
        List<Long> directPermIds = userPermissionRepository.findByUserId(userId).stream()
                .map(SysUserPermission::getPermissionId).toList();

        // 2) 用户角色ID -> 角色权限ID（一次性批量，避免循环单查）
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId).distinct().toList();
        List<Long> rolePermIds = roleIds.isEmpty() ? List.of()
                : rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                        .map(SysRolePermission::getPermissionId).distinct().toList();

        // 合并全部权限ID，一次 IN 取出 permKey
        Set<Long> permIds = new HashSet<>(directPermIds);
        permIds.addAll(rolePermIds);
        Set<String> permKeys = permIds.isEmpty() ? Set.of()
                : permissionRepository.findAllById(permIds).stream()
                        .filter(p -> p.getIsDeleted() == 0)
                        .map(SysPermission::getPermKey)
                        .collect(Collectors.toSet());
        Set<String> immutable = Set.copyOf(permKeys);

        writeToCache(userId, immutable);
        return immutable;
    }

    /**
     * 使某用户的权限缓存失效（删除 Redis key），供上层「授权 / 撤权 / 角色变更」调用，
     * 实现权限变更即时生效。Redis 不可用时该方法是安全的空操作。
     *
     * @param userId 目标用户ID
     */
    public void invalidateUser(Long userId) {
        try {
            stringRedisTemplate.delete(CACHE_KEY_PREFIX + userId);
        } catch (DataAccessException ex) {
            // Redis 不可用：无需主动失效，下次请求将回落到直查库
        }
    }

    /** 读缓存：命中返回权限点集合；未命中或 Redis 不可用返回 null */
    private Set<String> readFromCache(Long userId) {
        try {
            String value = stringRedisTemplate.opsForValue().get(CACHE_KEY_PREFIX + userId);
            if (value == null) {
                return null;
            }
            if (value.isEmpty()) {
                return Set.of();
            }
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toSet());
        } catch (DataAccessException ex) {
            // Redis 连接失败/命令异常 → 降级为直查库（不写缓存）
            return null;
        }
    }

    /** 写缓存：可能失败（如无 Redis）时静默降级，下次请求继续直查库 */
    private void writeToCache(Long userId, Set<String> permKeys) {
        try {
            stringRedisTemplate.opsForValue().set(
                    CACHE_KEY_PREFIX + userId,
                    String.join(",", permKeys),
                    CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS);
        } catch (DataAccessException ex) {
            // Redis 不可用 → 静默降级，权限判断仍走直查库
        }
    }
}