package cc.azuramc.bedwars.dashboard.service;

import cc.azuramc.bedwars.dashboard.entity.Server;
import cc.azuramc.bedwars.dashboard.repository.ServerRepository;
import cc.azuramc.bedwars.dashboard.util.JedisUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Redis Pub/Sub 服务
 * 管理 Redis 订阅、消息处理和频道管理
 *
 * @author An5w1r@163.com
 */
@Slf4j
@Service
public class PubSubService {

    // 订阅线程池
    private final ExecutorService subscriptionExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "PubSub-Subscription-" + System.currentTimeMillis());
        thread.setDaemon(true);
        return thread;
    });
    // 当前订阅的频道
    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();
    // 订阅任务映射
    private final ConcurrentHashMap<String, CompletableFuture<Void>> subscriptionTasks = new ConcurrentHashMap<>();
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private PubSubHandler pubSubHandler;
    @Autowired
    private AutoReplyService autoReplyService;

    /**
     * 应用完全启动后自动订阅所有服务器频道
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[PubSubService] 启动 Redis Pub/Sub 服务");

        CompletableFuture.runAsync(this::subscribeToAllServerChannels, subscriptionExecutor);
    }

    /**
     * 订阅所有服务器频道
     */
    public void subscribeToAllServerChannels() {
        try {
            List<Server> servers = StreamSupport.stream(serverRepository.findAll().spliterator(), false)
                    .toList();
            Set<String> channels = servers.stream()
                    .map(Server::getChannelId)
                    .filter(channelId -> channelId != null && !channelId.trim().isEmpty())
                    .collect(Collectors.toSet());

            if (channels.isEmpty()) {
                log.info("[PubSubService] 没有找到需要订阅的频道");
                return;
            }

            log.info("[PubSubService] 开始订阅 {} 个频道: {}", channels.size(), channels);

            for (String channel : channels) {
                subscribeToChannel(channel);
            }

        } catch (Exception e) {
            log.error("[PubSubService] 订阅所有服务器频道失败", e);
        }
    }

    /**
     * 订阅单个频道
     *
     * @param channel 频道名称
     */
    public void subscribeToChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            log.warn("[PubSubService] 频道名称为空，跳过订阅");
            return;
        }

        if (subscribedChannels.contains(channel)) {
            log.debug("[PubSubService] 频道 [{}] 已经订阅，跳过", channel);
            return;
        }

        try {
            CompletableFuture<Void> subscriptionTask = CompletableFuture.runAsync(() -> {
                Jedis jedis = null;
                try {
                    jedis = jedisUtil.getJedis();
                    if (jedis != null) {
                        subscribedChannels.add(channel);
                        log.info("[PubSubService] 开始订阅频道: {}", channel);

                        // 这里会阻塞直到取消订阅
                        jedis.subscribe(pubSubHandler, channel);
                    }
                } catch (Exception e) {
                    log.error("[PubSubService] 订阅频道 [{}] 失败: {}", channel, e.getMessage());
                    subscribedChannels.remove(channel);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }, subscriptionExecutor);

            subscriptionTasks.put(channel, subscriptionTask);

        } catch (Exception e) {
            log.error("[PubSubService] 创建订阅任务失败 - 频道: {}", channel, e);
        }
    }

    /**
     * 取消订阅频道
     *
     * @param channel 频道名称
     */
    public void unsubscribeFromChannel(String channel) {
        if (channel == null || !subscribedChannels.contains(channel)) {
            log.debug("[PubSubService] 频道 [{}] 未订阅或不存在", channel);
            return;
        }

        try {
            // 取消订阅
            pubSubHandler.unsubscribe(channel);
            subscribedChannels.remove(channel);

            // 取消订阅任务
            CompletableFuture<Void> task = subscriptionTasks.remove(channel);
            if (task != null && !task.isDone()) {
                task.cancel(true);
            }

            log.info("[PubSubService] 已取消订阅频道: {}", channel);
        } catch (Exception e) {
            log.error("[PubSubService] 取消订阅频道 [{}] 失败", channel, e);
        }
    }

    /**
     * 重新订阅频道（用于服务器配置更新后）
     */
    public void resubscribeAllChannels() {
        log.info("[PubSubService] 重新订阅所有频道");

        // 取消所有当前订阅
        Set<String> currentChannels = Set.copyOf(subscribedChannels);
        for (String channel : currentChannels) {
            unsubscribeFromChannel(channel);
        }

        // 等待一段时间确保取消订阅完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 重新订阅
        subscribeToAllServerChannels();
    }

    /**
     * 发送消息到频道
     *
     * @param channel 频道名称
     * @param message 消息内容
     * @return 发送是否成功
     */
    public boolean sendMessage(String channel, String message) {
        return autoReplyService.sendManualMessage(channel, message);
    }

    /**
     * 获取当前订阅的频道列表
     *
     * @return 订阅的频道集合
     */
    public Set<String> getSubscribedChannels() {
        return Set.copyOf(subscribedChannels);
    }

    /**
     * 检查频道是否已订阅
     *
     * @param channel 频道名称
     * @return 是否已订阅
     */
    public boolean isChannelSubscribed(String channel) {
        return subscribedChannels.contains(channel);
    }

    /**
     * 获取订阅状态信息
     *
     * @return 订阅状态信息
     */
    public String getSubscriptionStatus() {
        return String.format("当前订阅频道数: %d, 频道列表: %s",
                subscribedChannels.size(), subscribedChannels);
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void shutdown() {
        log.info("[PubSubService] 关闭 Redis Pub/Sub 服务");

        try {
            // 取消所有订阅
            if (pubSubHandler.isSubscribed()) {
                pubSubHandler.unsubscribe();
            }

            // 取消所有订阅任务
            subscriptionTasks.values().forEach(task -> {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            });

            // 关闭线程池
            subscriptionExecutor.shutdown();

        } catch (Exception e) {
            log.error("[PubSubService] 关闭服务时发生错误", e);
        }
    }
}
