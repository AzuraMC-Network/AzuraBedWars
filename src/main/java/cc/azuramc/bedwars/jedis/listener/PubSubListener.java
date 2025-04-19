package cc.azuramc.bedwars.jedis.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.JedisPubSubHandler;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Redis PubSub监听器
 * 用于处理Redis的发布订阅功能
 */
public class PubSubListener implements Runnable {
    // 常量定义
    private static final int RECONNECT_DELAY = 5; // 重连延迟(秒)
    private static final String SERVER_NAME_QUERY_PREFIX = "ServerManage.ServerNameQuery.";
    private static final String RUN_COMMAND_CHANNEL = "ServerManage.RunCommand";

    // 成员变量
    private JedisPubSubHandler jedisPubSubHandler;
    private final Set<String> addedChannels = new HashSet<>();

    /**
     * 运行监听器
     * 处理Redis订阅和重连逻辑
     */
    @Override
    public void run() {
        boolean connectionBroken = false;
        
        try (Jedis jedis = JedisUtil.getJedis()) {
            try {
                initializePubSub(jedis);
            } catch (Exception e) {
                handleSubscriptionError(e);
                connectionBroken = true;
            }
        } catch (JedisConnectionException e) {
            handleConnectionError();
        }

        if (connectionBroken) {
            run();
        }
    }

    /**
     * 初始化PubSub订阅
     * @param jedis Jedis连接
     */
    private void initializePubSub(Jedis jedis) {
        jedisPubSubHandler = new JedisPubSubHandler(AzuraBedWars.getInstance());
        addedChannels.add(SERVER_NAME_QUERY_PREFIX + IPUtil.getLocalIp());
        addedChannels.add(RUN_COMMAND_CHANNEL);
        jedis.subscribe(jedisPubSubHandler, addedChannels.toArray(new String[0]));
    }

    /**
     * 处理订阅错误
     * @param e 异常
     */
    private void handleSubscriptionError(Exception e) {
        Bukkit.getLogger().log(Level.WARNING, "PubSub订阅错误,尝试恢复", e);
        try {
            if (jedisPubSubHandler != null) {
                jedisPubSubHandler.unsubscribe();
            }
        } catch (Exception ignored) {
            // 忽略取消订阅时的错误
        }
    }

    /**
     * 处理连接错误
     */
    private void handleConnectionError() {
        Bukkit.getLogger().info("PubSub连接错误,将在" + RECONNECT_DELAY + "秒后重试");
        AzuraBedWars.getInstance().getServer().getScheduler()
            .runTaskTimerAsynchronously(
                AzuraBedWars.getInstance(), 
                this, 
                0, 
                20 * RECONNECT_DELAY
            );
    }

    /**
     * 添加订阅频道
     * @param channels 要添加的频道
     */
    public void addChannel(String... channels) {
        addedChannels.addAll(Arrays.asList(channels));
        if (jedisPubSubHandler != null) {
            jedisPubSubHandler.subscribe(channels);
        }
        Bukkit.getLogger().info("频道注册成功: " + Arrays.toString(channels));
    }

    /**
     * 移除订阅频道
     * @param channels 要移除的频道
     */
    public void removeChannel(String... channels) {
        Arrays.asList(channels).forEach(addedChannels::remove);
        if (jedisPubSubHandler != null) {
            jedisPubSubHandler.unsubscribe(channels);
        }
    }

    /**
     * 停止所有订阅
     */
    public void poison() {
        addedChannels.clear();
        if (jedisPubSubHandler != null) {
            jedisPubSubHandler.unsubscribe();
        }
    }
}
