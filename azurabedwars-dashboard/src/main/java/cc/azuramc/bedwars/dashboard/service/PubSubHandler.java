package cc.azuramc.bedwars.dashboard.service;

import cc.azuramc.bedwars.dashboard.entity.ChannelMessage;
import cc.azuramc.bedwars.dashboard.repository.ChannelMessageRepository;
import cc.azuramc.bedwars.dashboard.repository.ServerRepository;
import cc.azuramc.bedwars.dashboard.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPubSub;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Redis Pub/Sub 消息处理器
 * 处理从 Redis 订阅频道接收到的消息
 *
 * @author An5w1r@163.com
 */
@Slf4j
@Component
public class PubSubHandler extends JedisPubSub {

    @Autowired
    private ChannelMessageRepository channelMessageRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private AutoReplyService autoReplyService;

    /**
     * 当收到消息时的处理方法
     *
     * @param channel 消息频道
     * @param message 消息内容
     */
    @Override
    public void onMessage(String channel, String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                log.debug("收到空消息，忽略处理 - 频道: {}", channel);
                return;
            }

            log.info("[PubSub] 收到消息 - 频道: {}, 内容: {}", channel, message);

            // 异步保存接收到的消息
            CompletableFuture.runAsync(() -> saveReceivedMessage(channel, message));

            // 处理特殊消息类型
            handleSpecialMessage(channel, message);

        } catch (Exception e) {
            log.error("[PubSub] 处理消息时发生错误 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage(), e);
        }
    }

    /**
     * 当订阅频道时的回调
     *
     * @param channel            频道名称
     * @param subscribedChannels 已订阅的频道数量
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log.info("[PubSub] 成功订阅频道: {}, 当前订阅频道数: {}", channel, subscribedChannels);
    }

    /**
     * 当取消订阅频道时的回调
     *
     * @param channel            频道名称
     * @param subscribedChannels 剩余订阅的频道数量
     */
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        log.info("[PubSub] 取消订阅频道: {}, 剩余订阅频道数: {}", channel, subscribedChannels);
    }

    /**
     * 当订阅模式时的回调
     *
     * @param pattern            模式
     * @param subscribedChannels 已订阅的频道数量
     */
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        log.info("[PubSub] 成功订阅模式: {}, 当前订阅频道数: {}", pattern, subscribedChannels);
    }

    /**
     * 当取消订阅模式时的回调
     *
     * @param pattern            模式
     * @param subscribedChannels 剩余订阅的频道数量
     */
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        log.info("[PubSub] 取消订阅模式: {}, 剩余订阅频道数: {}", pattern, subscribedChannels);
    }

    /**
     * 当通过模式接收到消息时的回调
     *
     * @param pattern 模式
     * @param channel 频道
     * @param message 消息
     */
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.info("[PubSub] 通过模式 [{}] 在频道 [{}] 收到消息: {}", pattern, channel, message);
        onMessage(channel, message);
    }

    /**
     * 保存接收到的消息
     *
     * @param channel 频道名称
     * @param message 消息内容
     */
    private void saveReceivedMessage(String channel, String message) {
        try {
            ChannelMessage channelMessage = ChannelMessage.builder()
                    .channelName(channel)
                    .messageContent(message)
                    .messageType(ChannelMessage.MessageType.RECEIVED)
                    .messageTime(LocalDateTime.now())
                    .isAutoReply(false)
                    .build();

            channelMessageRepository.save(channelMessage);
            log.debug("已保存接收消息 - 频道: {}, 消息ID: {}", channel, channelMessage.getId());
        } catch (Exception e) {
            log.error("保存接收消息失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
        }
    }

    /**
     * 处理特殊消息类型
     *
     * @param channel 频道名称
     * @param message 消息内容
     */
    private void handleSpecialMessage(String channel, String message) {
        try {
            // 处理 request 消息
            if ("request".equalsIgnoreCase(message.trim())) {
                log.info("[PubSub] 收到 request 消息，触发自动回复 - 频道: {}", channel);
                autoReplyService.handleRequestMessage(channel);
            }
            // 可以在这里添加其他特殊消息的处理逻辑
        } catch (Exception e) {
            log.error("处理特殊消息失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
        }
    }
}
