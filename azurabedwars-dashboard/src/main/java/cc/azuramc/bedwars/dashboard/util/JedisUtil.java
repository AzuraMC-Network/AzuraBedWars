package cc.azuramc.bedwars.dashboard.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Redis 工具类
 * 提供 Redis 连接管理和消息发布功能
 *
 * @author An5w1r@163.com
 */
@Slf4j
@Component
public class JedisUtil {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 5000;

    @Autowired
    private JedisPool jedisPool;

    /**
     * 获取 Jedis 连接
     *
     * @return Jedis 连接实例
     * @throws RuntimeException 当连接失败时抛出异常
     */
    public Jedis getJedis() {
        return getJedisWithRetry(0);
    }

    /**
     * 带重试机制的获取 Jedis 连接
     *
     * @param retryCount 当前重试次数
     * @return Jedis 连接实例
     */
    private Jedis getJedisWithRetry(int retryCount) {
        try {
            return jedisPool.getResource();
        } catch (JedisConnectionException e) {
            log.error("获取 Redis 连接失败: {}", e.getMessage());
            if (retryCount < MAX_RETRIES) {
                log.info("将在 {} 秒后重试连接... (尝试 {}/{})", RETRY_DELAY / 1000, retryCount + 1, MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("连接被中断", ie);
                }
                return getJedisWithRetry(retryCount + 1);
            }
            throw new RuntimeException("Redis 连接失败，已达到最大重试次数", e);
        }
    }

    /**
     * 发布消息到指定频道
     *
     * @param channel 频道名称
     * @param message 消息内容
     * @return 接收到消息的订阅者数量
     */
    public Long publish(String channel, String message) {
        try (Jedis jedis = getJedis()) {
            Long result = jedis.publish(channel, message);
            log.info("发布消息到频道 [{}]: {}, 接收者数量: {}", channel, message, result);
            return result;
        } catch (Exception e) {
            log.error("发布消息到 Redis 失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
            throw new RuntimeException("发布消息失败", e);
        }
    }

    /**
     * 测试 Redis 连接
     *
     * @return 连接是否正常
     */
    public boolean testConnection() {
        try (Jedis jedis = getJedis()) {
            String response = jedis.ping();
            log.info("Redis 连接测试成功: {}", response);
            return "PONG".equals(response);
        } catch (Exception e) {
            log.error("Redis 连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 关闭连接池
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            log.info("Redis 连接池已关闭");
        }
    }
}