package cc.azuramc.bedwars.jedis.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.JedisPubSubHandler;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import cc.azuramc.bedwars.jedis.util.JedisUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    private ExecutorService executorService;
    private Future<?> subscribeTask;

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
            // 初始化线程池（单线程执行器，因为Redis订阅需要持续运行）
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor(r -> {
                    Thread thread = new Thread(r, "Redis-Subscribe-Thread");
                    thread.setDaemon(true);
                    return thread;
                });
            }
            
            jedisPubSubHandler = new JedisPubSubHandler(AzuraBedWars.getInstance());
            
            // 添加所有需要的频道
            String channel = "AZURA.BW." + IPUtil.getLocalIp();
            addedChannels.add(channel);
            
            // 记录订阅的频道
            LoggerUtil.info("正在订阅以下频道: " + String.join(", ", addedChannels));
            
            // 使用线程池执行订阅操作
            subscribeTask = executorService.submit(() -> {
                try (Jedis jedis = JedisUtil.getJedis()) {
                    jedis.subscribe(jedisPubSubHandler, addedChannels.toArray(new String[0]));
                } catch (JedisConnectionException e) {
                    LoggerUtil.error("Redis连接失败，将在" + RECONNECT_DELAY + "秒后重试");
                    scheduleReconnect();
                } catch (Exception e) {
                    LoggerUtil.error("初始化PubSub订阅失败");
                }
            });
        } catch (Exception e) {
            LoggerUtil.error("初始化PubSub订阅失败");
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
        LoggerUtil.info("频道注册成功: " + Arrays.toString(channels));
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
        
        // 取消订阅任务
        if (subscribeTask != null && !subscribeTask.isDone()) {
            subscribeTask.cancel(true);
        }
        
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // 等待任务完成，如果超时则强制关闭
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        isInitialized = false;
    }
}
