package cc.azuramc.bedwars.jedis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.bukkit.Bukkit;
import java.util.logging.Level;

/**
 * Redis工具类
 * 用于管理Redis连接池和发布消息
 * @author an5w1r@163.com
 */
public class JedisUtil {

    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;
    private static final int MAX_TOTAL = 2000;
    /**
     * 5秒超时
     */
    private static final int TIMEOUT = 5000;
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;
    /**
     * 重试延迟(毫秒)
     */
    private static final long RETRY_DELAY = 5000;

    private static JedisPool pool = null;
    private static int retryCount = 0;

    /**
     * 获取Jedis连接
     * @return Jedis连接实例
     */
    public static Jedis getJedis() {
        if (pool == null) {
            initializePool();
        }
        
        try {
            return pool.getResource();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "获取Redis连接失败: " + e.getMessage());
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                Bukkit.getLogger().info("将在" + (RETRY_DELAY/1000) + "秒后重试连接... (尝试 " + retryCount + "/" + MAX_RETRIES + ")");
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return getJedis();
            }
            throw e;
        }
    }

    /**
     * 初始化连接池
     */
    private static synchronized void initializePool() {
        if (pool == null) {
            try {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(MAX_TOTAL);
                config.setMaxWaitMillis(TIMEOUT);
                config.setTestOnBorrow(true);
                config.setTestWhileIdle(true);
                
                pool = new JedisPool(config, REDIS_HOST, REDIS_PORT, TIMEOUT);
                // 测试连接
                try (Jedis jedis = pool.getResource()) {
                    jedis.ping();
                    Bukkit.getLogger().info("Redis连接成功: " + REDIS_HOST + ":" + REDIS_PORT);
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "初始化Redis连接池失败: " + e.getMessage());
                pool = null;
                throw e;
            }
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
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "发布消息到Redis失败: " + e.getMessage());
            throw e;
        }
    }
}
