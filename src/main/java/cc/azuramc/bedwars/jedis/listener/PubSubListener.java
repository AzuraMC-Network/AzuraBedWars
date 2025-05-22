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
 *
 * @author an5w1r@163.com
 */
public class PubSubListener implements Runnable {
    /**
     * 重连延迟(秒)
     */
    private static final int RECONNECT_DELAY = 5;

    private JedisPubSubHandler jedisPubSubHandler;
    private final Set<String> addedChannels = new HashSet<>();
    private boolean isInitialized = false;
    private Thread subscribeThread;

    /**
     * 运行监听器
     * 处理Redis订阅和重连逻辑
     */
    @Override
    public void run() {
        if (!isInitialized) {
            initializePubSub();
            isInitialized = true;
        }
    }

    /**
     * 初始化PubSub订阅
     */
    private void initializePubSub() {
        try {
            jedisPubSubHandler = new JedisPubSubHandler(AzuraBedWars.getInstance());
            
            // 添加所有需要的频道
            String channel = "AZURA.BW." + IPUtil.getLocalIp();
            addedChannels.add(channel);
            
            // 记录订阅的频道
            Bukkit.getLogger().info("正在订阅以下频道: " + String.join(", ", addedChannels));
            
            // 在单独的线程中执行订阅操作
            subscribeThread = new Thread(() -> {
                try (Jedis jedis = JedisUtil.getJedis()) {
                    jedis.subscribe(jedisPubSubHandler, addedChannels.toArray(new String[0]));
                } catch (JedisConnectionException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Redis连接失败，将在" + RECONNECT_DELAY + "秒后重试", e);
                    scheduleReconnect();
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "初始化PubSub订阅失败", e);
                }
            }, "Redis-Subscribe-Thread");
            
            subscribeThread.start();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "初始化PubSub订阅失败", e);
        }
    }

    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        AzuraBedWars.getInstance().getServer().getScheduler()
            .runTaskLaterAsynchronously(
                AzuraBedWars.getInstance(), 
                this, 
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
        if (subscribeThread != null && subscribeThread.isAlive()) {
            subscribeThread.interrupt();
        }
        isInitialized = false;
    }
}
