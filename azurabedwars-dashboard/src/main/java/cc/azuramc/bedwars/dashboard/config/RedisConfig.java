package cc.azuramc.bedwars.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis 配置类
 * 配置 Jedis 连接池和相关参数
 *
 * @author An5w1r@163.com
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.redis.timeout:5000}")
    private int timeout;


    /**
     * 配置 Jedis 连接池
     *
     * @return JedisPool 实例
     */
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setTestOnReturn(false);
        config.setNumTestsPerEvictionRun(3);
        config.setJmxEnabled(false);

        return new JedisPool(config, host, port, timeout);
    }
}
