package ustb.hyy.app.backend.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法ID生成器（Twitter Snowflake）
 * <p>
 * ID结构（64位）：
 * - 1位符号位（始终为0）
 * - 41位时间戳（毫秒级，可使用约69年）
 * - 5位数据中心ID（支持32个数据中心）
 * - 5位工作机器ID（每个数据中心支持32台机器）
 * - 12位序列号（每毫秒支持4096个ID）
 * </p>
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Slf4j
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳（2025-01-01 00:00:00 UTC）
     */
    private static final long START_TIMESTAMP = 1735689600000L;

    /**
     * 数据中心ID占用位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 工作机器ID占用位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 序列号占用位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 数据中心ID最大值（31）
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 工作机器ID最大值（31）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 序列号最大值（4095）
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID左移位数（12位）
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数（17位）
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数（22位）
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 数据中心ID
     */
    private final long datacenterId;

    /**
     * 工作机器ID
     */
    private final long workerId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param datacenterId 数据中心ID（0-31）
     * @param workerId     工作机器ID（0-31）
     */
    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID不能大于%d或小于0", MAX_DATACENTER_ID));
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID不能大于%d或小于0", MAX_WORKER_ID));
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
        log.info("雪花算法ID生成器初始化成功，DatacenterId: {}, WorkerId: {}", datacenterId, workerId);
    }

    /**
     * 生成下一个ID（线程安全）
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // 小于5ms的回拨，等待两倍时间
                try {
                    wait(offset << 1);
                    timestamp = getCurrentTimestamp();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException(
                                String.format("时钟回拨，拒绝生成ID。回拨时长：%dms", offset));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("ID生成被中断", e);
                }
            } else {
                throw new RuntimeException(
                        String.format("时钟回拨，拒绝生成ID。回拨时长：%dms", offset));
            }
        }

        // 同一毫秒内，序列号自增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，阻塞到下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳 | 数据中心ID | 工作机器ID | 序列号
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳（毫秒）
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 阻塞到下一毫秒
     *
     * @param lastTimestamp 上次时间戳
     * @return 下一毫秒时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 解析ID，返回时间戳、数据中心ID、工作机器ID、序列号
     *
     * @param id 雪花算法生成的ID
     * @return [timestamp, datacenterId, workerId, sequence]
     */
    public static long[] parseId(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + START_TIMESTAMP;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & MAX_SEQUENCE;
        return new long[]{timestamp, datacenterId, workerId, sequence};
    }
}
