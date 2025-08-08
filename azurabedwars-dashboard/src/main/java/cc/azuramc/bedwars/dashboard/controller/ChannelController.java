package cc.azuramc.bedwars.dashboard.controller;

import cc.azuramc.bedwars.dashboard.entity.ChannelMessage;
import cc.azuramc.bedwars.dashboard.entity.ResponseMessage;
import cc.azuramc.bedwars.dashboard.repository.ChannelMessageRepository;
import cc.azuramc.bedwars.dashboard.service.AutoReplyService;
import cc.azuramc.bedwars.dashboard.service.PubSubService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 频道消息控制器
 * 提供频道消息管理和发送的 REST API
 *
 * @author An5w1r@163.com
 */
@Slf4j
@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    @Autowired
    private ChannelMessageRepository channelMessageRepository;

    @Autowired
    private PubSubService pubSubService;

    @Autowired
    private AutoReplyService autoReplyService;

    /**
     * 获取所有有消息记录的频道列表
     */
    @GetMapping
    public ResponseEntity<ResponseMessage<List<String>>> getAllChannels() {
        try {
            List<String> channels = channelMessageRepository.findDistinctChannelNames();
            return ResponseEntity.ok(ResponseMessage.success(channels));
        } catch (Exception e) {
            log.error("获取频道列表失败", e);
            return ResponseEntity.ok(ResponseMessage.error("获取频道列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定频道的消息列表（分页）
     */
    @GetMapping("/{channelName}/messages")
    public ResponseEntity<ResponseMessage<Page<ChannelMessage>>> getChannelMessages(
            @PathVariable String channelName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "messageTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ChannelMessage> messages = channelMessageRepository.findByChannelNameOrderByMessageTimeDesc(
                    channelName, pageable);

            return ResponseEntity.ok(ResponseMessage.success(messages));
        } catch (Exception e) {
            log.error("获取频道消息失败 - 频道: {}", channelName, e);
            return ResponseEntity.ok(ResponseMessage.error("获取频道消息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取频道消息统计信息
     */
    @GetMapping("/{channelName}/stats")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getChannelStats(
            @PathVariable String channelName) {
        try {
            long totalMessages = channelMessageRepository.countByChannelName(channelName);
            long receivedMessages = channelMessageRepository.countByChannelNameAndMessageType(
                    channelName, ChannelMessage.MessageType.RECEIVED);
            long sentMessages = channelMessageRepository.countByChannelNameAndMessageType(
                    channelName, ChannelMessage.MessageType.SENT);
            long autoReplies = channelMessageRepository.countByChannelNameAndIsAutoReply(
                    channelName, true);

            boolean isSubscribed = pubSubService.isChannelSubscribed(channelName);

            Map<String, Object> stats = Map.of(
                    "channelName", channelName,
                    "totalMessages", totalMessages,
                    "receivedMessages", receivedMessages,
                    "sentMessages", sentMessages,
                    "autoReplies", autoReplies,
                    "isSubscribed", isSubscribed
            );

            return ResponseEntity.ok(ResponseMessage.success(stats));
        } catch (Exception e) {
            log.error("获取频道统计失败 - 频道: {}", channelName, e);
            return ResponseEntity.ok(ResponseMessage.error("获取频道统计失败: " + e.getMessage()));
        }
    }

    /**
     * 手动发送消息到频道
     */
    @PostMapping("/{channelName}/send")
    public ResponseEntity<ResponseMessage<String>> sendMessage(
            @PathVariable String channelName,
            @RequestBody @Valid SendMessageRequest request) {
        try {
            boolean success = pubSubService.sendMessage(channelName, request.getMessage());

            if (success) {
                return ResponseEntity.ok(ResponseMessage.success("消息已发送到频道: " + channelName));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("消息发送失败"));
            }
        } catch (Exception e) {
            log.error("发送消息失败 - 频道: {}, 消息: {}", channelName, request.getMessage(), e);
            return ResponseEntity.ok(ResponseMessage.error("发送消息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新服务器的自动回复消息
     */
    @PutMapping("/auto-reply/{serverId}")
    public ResponseEntity<ResponseMessage<String>> updateAutoReply(
            @PathVariable Integer serverId,
            @RequestBody @Valid UpdateAutoReplyRequest request) {
        try {
            boolean success = autoReplyService.updateAutoReplyMessage(serverId, request.getDefaultMap());

            if (success) {
                return ResponseEntity.ok(ResponseMessage.success(
                        "服务器 " + serverId + " 的自动回复消息已更新"));
            } else {
                return ResponseEntity.ok(ResponseMessage.error("服务器不存在或更新失败"));
            }
        } catch (Exception e) {
            log.error("更新自动回复消息失败 - 服务器ID: {}", serverId, e);
            return ResponseEntity.ok(ResponseMessage.error("更新自动回复消息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取订阅状态
     */
    @GetMapping("/subscription/status")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> getSubscriptionStatus() {
        try {
            String status = pubSubService.getSubscriptionStatus();
            var subscribedChannels = pubSubService.getSubscribedChannels();

            Map<String, Object> result = Map.of(
                    "status", status,
                    "subscribedChannels", subscribedChannels,
                    "subscribedCount", subscribedChannels.size()
            );

            return ResponseEntity.ok(ResponseMessage.success(result));
        } catch (Exception e) {
            log.error("获取订阅状态失败", e);
            return ResponseEntity.ok(ResponseMessage.error("获取订阅状态失败: " + e.getMessage()));
        }
    }

    /**
     * 重新订阅所有频道
     */
    @PostMapping("/subscription/refresh")
    public ResponseEntity<ResponseMessage<String>> refreshSubscription() {
        try {
            pubSubService.resubscribeAllChannels();
            return ResponseEntity.ok(ResponseMessage.success("已重新订阅所有频道"));
        } catch (Exception e) {
            log.error("重新订阅失败", e);
            return ResponseEntity.ok(ResponseMessage.error("重新订阅失败: " + e.getMessage()));
        }
    }

    /**
     * 清理指定频道的历史消息
     */
    @DeleteMapping("/{channelName}/messages")
    public ResponseEntity<ResponseMessage<String>> clearChannelMessages(
            @PathVariable String channelName,
            @RequestParam(required = false) Integer days) {
        try {
            long deletedCount;
            if (days != null && days > 0) {
                LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
                deletedCount = channelMessageRepository.deleteByChannelNameAndMessageTimeBefore(
                        channelName, cutoffTime);
            } else {
                deletedCount = channelMessageRepository.deleteByChannelName(channelName);
            }

            String message = days != null ?
                    String.format("已清理频道 %s 中 %d 天前的 %d 条消息", channelName, days, deletedCount) :
                    String.format("已清理频道 %s 中的 %d 条消息", channelName, deletedCount);

            return ResponseEntity.ok(ResponseMessage.success(message));
        } catch (Exception e) {
            log.error("清理频道消息失败 - 频道: {}", channelName, e);
            return ResponseEntity.ok(ResponseMessage.error("清理频道消息失败: " + e.getMessage()));
        }
    }

    /**
     * 发送消息请求体
     */
    @Data
    public static class SendMessageRequest {
        @NotBlank(message = "消息内容不能为空")
        private String message;

    }

    /**
     * 更新自动回复请求体
     */
    @Data
    public static class UpdateAutoReplyRequest {
        @NotBlank(message = "默认地图不能为空")
        private String defaultMap;

    }
}
