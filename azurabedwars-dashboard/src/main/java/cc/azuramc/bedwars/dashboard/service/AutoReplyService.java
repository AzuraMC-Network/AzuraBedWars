package cc.azuramc.bedwars.dashboard.service;

import cc.azuramc.bedwars.dashboard.entity.ChannelMessage;
import cc.azuramc.bedwars.dashboard.entity.Server;
import cc.azuramc.bedwars.dashboard.repository.ChannelMessageRepository;
import cc.azuramc.bedwars.dashboard.repository.ServerRepository;
import cc.azuramc.bedwars.dashboard.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author An5w1r@163.com
 */
@Slf4j
@Service
public class AutoReplyService {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ChannelMessageRepository channelMessageRepository;

    @Autowired
    private JedisUtil jedisUtil;

    /**
     * 处理 request 消息
     * 当收到 plugin 端的 request 消息时，自动回复默认地图信息
     *
     * @param channel 频道名称
     */
    public void handleRequestMessage(String channel) {
        try {
            // 查找对应的服务器配置
            Optional<Server> serverOpt = serverRepository.findByChannelId(channel);

            if (serverOpt.isPresent()) {
                Server server = serverOpt.get();
                String replyMessage = buildReplyMessage(server);

                // 发送回复消息
                sendAutoReply(channel, replyMessage);

                log.info("[AutoReply] 已自动回复 request 消息 - 频道: {}, 服务器: {}, 回复: {}",
                        channel, server.getDisplayName(), replyMessage);
            } else {
                // 如果没有找到对应的服务器配置，发送默认回复
                String defaultReply = "未找到服务器配置";
                sendAutoReply(channel, defaultReply);

                log.warn("[AutoReply] 未找到频道对应的服务器配置 - 频道: {}, 发送默认回复: {}", channel, defaultReply);
            }
        } catch (Exception e) {
            log.error("[AutoReply] 处理 request 消息失败 - 频道: {}, 错误: {}", channel, e.getMessage(), e);
        }
    }

    /**
     * 构建回复消息
     * 根据服务器配置构建要回复的消息内容
     *
     * @param server 服务器配置
     * @return 回复消息
     */
    private String buildReplyMessage(Server server) {
        // 这里可以根据需要自定义回复消息的格式
        // 目前返回默认地图信息
        return server.getDefaultMap();
    }

    /**
     * 发送自动回复消息
     *
     * @param channel 频道名称
     * @param message 回复消息内容
     */
    private void sendAutoReply(String channel, String message) {
        try {
            // 发布消息到 Redis
            jedisUtil.publish(channel, message);

            // 保存发送记录
            saveSentMessage(channel, message, true);

        } catch (Exception e) {
            log.error("[AutoReply] 发送自动回复失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
            throw e;
        }
    }

    /**
     * 保存发送的消息记录
     *
     * @param channel     频道名称
     * @param message     消息内容
     * @param isAutoReply 是否为自动回复
     */
    private void saveSentMessage(String channel, String message, boolean isAutoReply) {
        try {
            ChannelMessage channelMessage = ChannelMessage.builder()
                    .channelName(channel)
                    .messageContent(message)
                    .messageType(ChannelMessage.MessageType.SENT)
                    .messageTime(LocalDateTime.now())
                    .isAutoReply(isAutoReply)
                    .remark(isAutoReply ? "自动回复 request 消息" : "手动发送")
                    .build();

            channelMessageRepository.save(channelMessage);
            log.debug("已保存发送消息记录 - 频道: {}, 消息ID: {}, 自动回复: {}",
                    channel, channelMessage.getId(), isAutoReply);
        } catch (Exception e) {
            log.error("保存发送消息记录失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
        }
    }

    /**
     * 手动发送消息到频道
     *
     * @param channel 频道名称
     * @param message 消息内容
     * @return 发送是否成功
     */
    public boolean sendManualMessage(String channel, String message) {
        try {
            jedisUtil.publish(channel, message);
            saveSentMessage(channel, message, false);

            log.info("[ManualSend] 手动发送消息成功 - 频道: {}, 消息: {}", channel, message);
            return true;
        } catch (Exception e) {
            log.error("[ManualSend] 手动发送消息失败 - 频道: {}, 消息: {}, 错误: {}", channel, message, e.getMessage());
            return false;
        }
    }

    /**
     * 更新服务器的自动回复消息
     *
     * @param serverId      服务器ID
     * @param newDefaultMap 新的默认地图
     * @return 更新是否成功
     */
    public boolean updateAutoReplyMessage(Integer serverId, String newDefaultMap) {
        try {
            Optional<Server> serverOpt = serverRepository.findById(serverId);
            if (serverOpt.isPresent()) {
                Server server = serverOpt.get();
                server.setDefaultMap(newDefaultMap);
                serverRepository.save(server);

                log.info("[AutoReply] 更新服务器自动回复消息 - 服务器ID: {}, 新消息: {}", serverId, newDefaultMap);
                return true;
            } else {
                log.warn("[AutoReply] 未找到服务器 - ID: {}", serverId);
                return false;
            }
        } catch (Exception e) {
            log.error("[AutoReply] 更新自动回复消息失败 - 服务器ID: {}, 错误: {}", serverId, e.getMessage());
            return false;
        }
    }
}
