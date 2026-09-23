package com.example.backend.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 业务数据 Cache-Aside（旁路缓存）通用工具。
 *
 * <p>面向「查询多、写少、可容忍秒级延迟」的业务数据（如文档详情/配置/字典），
 * 统一封装 <b>先查缓存 → 未命中回源（loader）→ 写回缓存</b> 的旁路缓存链路，
 * 并提供 <b>写后删缓存</b> 的失效方法。落地自课件《7.2 Redis 缓存实战与 Cache-Aside 模式》。</p>
 *
 * <p><b>与既有约定一致</b>：复用 {@link PermissionAspect} 的 Redis 容错原则——所有读写以
 * try-catch 包裹 {@link DataAccessException}，Redis 不可用时<b>透明降级</b>为每次直调 loader，
 * 不写缓存，功能不受影响（仅性能略降）。</p>
 *
 * <p><b>防缓存穿透</b>：loader 返回 null（数据不存在）时，不写永久空，而是写入一个带短 TTL 的
 * 空标记（见 {@link #SENTINEL}），短时间屏蔽对不存在键的重复回源；该标记会在 TTL 到期后消失，
 * 避免永久性穿透。</p>
 *
 * <p><b>使用约定</b>：业务层调用方负责
 * <ul>
 *   <li>构造全局唯一 key（建议带业务前缀，如 {@code doc:detail:123}，勿与 {@code auth:*} 冲突）；</li>
 *   <li>写路径（create/update/delete）完成后调用 {@link #invalidate(String)} 删除缓存，保证一致性。</li>
 * </ul></p>
 */
@Component
@RequiredArgsConstructor
public class CacheAsideService {

    /** 空值缓存的短 TTL（秒）：数据不存在时短暂缓存，防止缓存穿透同时可自愈。 */
    private static final long EMPTY_TTL_SECONDS = 30L;

    /** 空标记：代表"该 key 在缓存中但实际无数据"，用于区分「命中为空」与「未命中需回源」。 */
    private static final String SENTINEL = "\u0000NB"; // NON-BREAKING sentinel

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 读缓存：命中（含空标记命中）返回数据；未命中走 loader 回源并写回，设置 TTL。
     *
     * @param key      全局唯一缓存键（业务前缀 + 业务ID）
     * @param type      目标类型（Class），用于 JSON 反序列化
     * @param loader    回源函数（查询数据库）；返回 null 表示数据不存在
     * @param ttl       缓存有效期
     * @param <T>       返回值类型
     * @return 缓存或 loader 的数据；loader 返回 null 时返回 null
     */
    public <T> T get(String key, Class<T> type, Supplier<T> loader, Duration ttl) {
        String cached = readRaw(key);
        if (cached != null) {
            return parseOrNull(cached, type, key);
        }
        T value = runLoader(loader);
        writeBack(key, value, ttl.toSeconds());
        return value;
    }

    /**
     * 读缓存（列表）：未命中回源，返回 List 类型。
     *
     * @param key        全局唯一缓存键
     * @param elementType 列表元素类型
     * @param loader      回源函数
     * @param ttl         缓存有效期
     * @param <T>         元素类型
     * @return 缓存或 loader 数据
     */
    public <T> List<T> getList(String key, Class<T> elementType, Supplier<List<T>> loader, Duration ttl) {
        String cached = readRaw(key);
        if (cached != null) {
            return parseListOrNull(cached, elementType, key);
        }
        List<T> value = loader.get();
        if (value == null) {
            value = List.of();
        }
        writeBack(key, value, ttl.toSeconds());
        return value;
    }

    /**
     * 写后删缓存：业务 create/update/delete 成功后调用，保证与数据库一致。
     * Redis 不可用时为安全空操作。
     */
    public void invalidate(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (DataAccessException ex) {
            // Redis 不可用：无需失效，下次读取将回落到直查库
        }
    }

    /* ---------- 私有实现 ---------- */

    /** 读原始值；未命中或 Redis 不可用返回 null */
    private String readRaw(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    /** 回源并回填缓存；value 为 null 时写空标记（短 TTL），防止穿透 */
    private void writeBack(String key, Object value, long ttlSeconds) {
        try {
            if (value == null) {
                stringRedisTemplate.opsForValue()
                        .set(key, SENTINEL, EMPTY_TTL_SECONDS, TimeUnit.SECONDS);
                return;
            }
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (DataAccessException | JsonProcessingException ex) {
            // 降级：写入失败不影响功能，下次回源直查
        }
    }

    private <T> T parseOrNull(String raw, Class<T> type, String key) {
        if (SENTINEL.equals(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, type);
        } catch (JsonProcessingException ex) {
            // 数据损坏（如序列化格式变更）：视为未命中并回源
            invalidate(key);
            return null;
        }
    }

    private <T> List<T> parseListOrNull(String raw, Class<T> elementType, String key) {
        if (SENTINEL.equals(raw)) {
            return List.of();
        }
        try {
            CollectionType colType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, elementType);
            return objectMapper.readValue(raw, colType);
        } catch (JsonProcessingException ex) {
            invalidate(key);
            return List.of();
        }
    }

    /** 回源调用：loader 自身抛异常时直接上抛（业务错误不应被缓存吞掉） */
    private <T> T runLoader(Supplier<T> loader) {
        return loader.get();
    }
}