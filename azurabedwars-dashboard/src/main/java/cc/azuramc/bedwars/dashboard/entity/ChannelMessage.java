package cc.azuramc.bedwars.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 频道消息实体类
 * 用于存储频道通信的消息记录
 *
 * @author An5w1r@163.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "channel_message")
public class ChannelMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 频道名称
     */
    @Column(name = "channel_name", nullable = false)
    private String channelName;

    /**
     * 消息内容
     */
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    /**
     * 消息类型：RECEIVED(接收), SENT(发送)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    /**
     * 消息时间
     */
    @Column(name = "message_time", nullable = false)
    private LocalDateTime messageTime;

    /**
     * 是否为自动回复消息
     */
    @Builder.Default
    @Column(name = "is_auto_reply", nullable = false)
    private Boolean isAutoReply = false;

    /**
     * 备注信息
     */
    @Column(name = "remark")
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (messageTime == null) {
            messageTime = LocalDateTime.now();
        }
        if (isAutoReply == null) {
            isAutoReply = false;
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 接收的消息
         */
        RECEIVED,
        /**
         * 发送的消息
         */
        SENT
    }
}
