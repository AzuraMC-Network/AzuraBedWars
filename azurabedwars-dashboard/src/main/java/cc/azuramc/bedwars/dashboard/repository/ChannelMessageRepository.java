package cc.azuramc.bedwars.dashboard.repository;

import cc.azuramc.bedwars.dashboard.entity.ChannelMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
@Repository
public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, Long> {

    /**
     * 根据频道名称查找消息
     *
     * @param channelName 频道名称
     * @param pageable    分页参数
     * @return 分页的消息列表
     */
    Page<ChannelMessage> findByChannelNameOrderByMessageTimeDesc(String channelName, Pageable pageable);

    /**
     * 根据频道名称查找最近的消息
     *
     * @param channelName 频道名称
     * @param limit       限制数量
     * @return 消息列表
     */
    @Query("SELECT cm FROM ChannelMessage cm WHERE cm.channelName = :channelName ORDER BY cm.messageTime DESC LIMIT :limit")
    List<ChannelMessage> findRecentMessagesByChannel(@Param("channelName") String channelName, @Param("limit") int limit);

    /**
     * 根据消息类型查找消息
     *
     * @param channelName 频道名称
     * @param messageType 消息类型
     * @param pageable    分页参数
     * @return 分页的消息列表
     */
    Page<ChannelMessage> findByChannelNameAndMessageTypeOrderByMessageTimeDesc(
            String channelName, ChannelMessage.MessageType messageType, Pageable pageable);

    /**
     * 根据时间范围查找消息
     *
     * @param channelName 频道名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param pageable    分页参数
     * @return 分页的消息列表
     */
    Page<ChannelMessage> findByChannelNameAndMessageTimeBetweenOrderByMessageTimeDesc(
            String channelName, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 统计频道消息数量
     *
     * @param channelName 频道名称
     * @return 消息数量
     */
    long countByChannelName(String channelName);

    /**
     * 统计频道中指定类型的消息数量
     *
     * @param channelName 频道名称
     * @param messageType 消息类型
     * @return 消息数量
     */
    long countByChannelNameAndMessageType(String channelName, ChannelMessage.MessageType messageType);

    /**
     * 删除指定时间之前的消息
     *
     * @param beforeTime 时间点
     * @return 删除的消息数量
     */
    long deleteByMessageTimeBefore(LocalDateTime beforeTime);

    /**
     * 获取所有不同的频道名称
     */
    @Query("SELECT DISTINCT cm.channelName FROM ChannelMessage cm ORDER BY cm.channelName")
    List<String> findDistinctChannelNames();

    /**
     * 删除指定频道的所有消息
     *
     * @param channelName 频道名称
     * @return 删除的消息数量
     */
    long deleteByChannelName(String channelName);

    /**
     * 删除指定频道在指定时间之前的消息
     *
     * @param channelName 频道名称
     * @param beforeTime  时间界限
     * @return 删除的消息数量
     */
    long deleteByChannelNameAndMessageTimeBefore(String channelName, LocalDateTime beforeTime);

    /**
     * 统计指定频道的自动回复消息数量
     *
     * @param channelName 频道名称
     * @param isAutoReply 是否为自动回复
     * @return 消息数量
     */
    long countByChannelNameAndIsAutoReply(String channelName, boolean isAutoReply);
}
