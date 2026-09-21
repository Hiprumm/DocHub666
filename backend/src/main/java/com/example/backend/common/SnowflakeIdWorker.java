package com.example.backend.common;

/**
 * 雪花算法 ID 生成器（单机简化版）。
 *
 * <p>依据《JPA 企业级实体类全景设计》规范：分布式主键采用雪花算法产生的
 * {@code Long} 数字 ID，实体层对外序列化为 {@code String}，避免 JS 端精度丢失。</p>
 */
public final class SnowflakeIdWorker {

    /** 起始时间戳（2024-01-01 00:00:00 UTC） */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 各部分占用位数 */
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    /** 最大值 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(1L, 1L);

    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 必须在 [0, " + MAX_WORKER_ID + "] 之间");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 必须在 [0, " + MAX_DATACENTER_ID + "] 之间");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /** 获取全局单例生成器 */
    public static SnowflakeIdWorker getInstance() {
        return INSTANCE;
    }

    /** 生成下一个全局唯一 ID */
    public static synchronized long nextId() {
        return INSTANCE.generateId();
    }

    private synchronized long generateId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("系统时钟回拨，拒绝生成 ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}