package com.github.dadiyang.autologging.aop.util;


import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法工具类
 *
 * @author Twitter_Snowflake
 */
public class SnowFlakeIdUtils {
    private static final SnowFlakeIdUtils INSTANCE;

    static {
        // 生成默认随机的 workerId 和 数据中心id
        int dataCenterId;
        try {
            // 数据中心使用机器 hostName 的哈希
            dataCenterId = Math.abs(InetAddress.getLocalHost().getHostName().hashCode()) & 31;
        } catch (Exception e) {
            // 获取不到则使用随机数
            dataCenterId = ThreadLocalRandom.current().nextInt(32);
        }
        // workerId 使用随机数
        int workerId = ThreadLocalRandom.current().nextInt(32);
        INSTANCE = new SnowFlakeIdUtils(workerId, dataCenterId);
    }

    /**
     * 开始时间截 (2019-04-01)
     */
    private static final long TWEPOCH = 1554106304301L;
    /**
     * 机器id所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;
    /**
     * 数据标识id所占的位数
     */
    private static final long DATACENTER_ID_BITS = 5L;
    /**
     * 序列在id中占的位数
     */
    private static final long SEQUENCE_BITS = 12L;
    /**
     * 机器ID向左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /**
     * 数据标识id向左移17位(12+5)
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /**
     * 时间截向左移22位(5+5+12)
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
    /**
     * 工作机器ID(0~31)
     */
    private long workerId = 0;
    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId = 0;
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 通过单例的静态方法获取下一个id
     *
     * @return 生成的id
     */
    public static long next() {
        return INSTANCE.nextId();
    }

    public SnowFlakeIdUtils(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return 生成的id
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        //上次生成ID的时间截
        lastTimestamp = timestamp;
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}