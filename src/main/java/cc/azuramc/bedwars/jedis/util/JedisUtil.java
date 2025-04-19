package cc.azuramc.bedwars.jedis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis工具类
 * 用于管理Redis连接池和发布消息
 */
public class JedisUtil {
    // Redis连接池配置
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6380;
    private static final int MAX_TOTAL = 2000;
    private static final int TIMEOUT = 0;

    private static JedisPool pool = null;

    /**
     * 获取Jedis连接
     * @return Jedis连接实例
     */
    public static Jedis getJedis() {
        if (pool == null) {
            initializePool();
        }
        return pool.getResource();
    }

    /**
     * 初始化连接池
     */
    private static synchronized void initializePool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_TOTAL);
            pool = new JedisPool(config, REDIS_HOST, REDIS_PORT, TIMEOUT);
        }
    }

    /**
     * 发布消息到指定频道
     * @param channel 频道名称
     * @param message 消息内容
     */
    public static void publish(String channel, String message) {
        try (Jedis jedis = getJedis()) {
            jedis.publish(channel, message);
        }
    }
}
