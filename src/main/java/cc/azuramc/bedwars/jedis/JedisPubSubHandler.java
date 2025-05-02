package cc.azuramc.bedwars.jedis;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.map.MapLoadManager;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import cc.azuramc.bedwars.jedis.util.IPUtil;
import redis.clients.jedis.JedisPubSub;
import org.bukkit.Bukkit;
import java.util.logging.Level;

/**
 * Redis PubSub消息处理器
 * 用于处理从Redis订阅频道接收到的消息，包括地图加载等游戏相关功能
 *
 * @author an5w1r@163.com
 */
public class JedisPubSubHandler extends JedisPubSub {
    
    private static final String LOG_PREFIX = "[JedisPubSub] ";
    private static final String MAP_LOAD_CHANNEL_PREFIX = "AZURA.BW.";
    
    private final MapLoadManager mapLoadManager;
    
    public JedisPubSubHandler(AzuraBedWars plugin) {
        this.mapLoadManager = MapLoadManager.getInstance(plugin);
    }

    /**
     * 当收到消息时的处理方法
     * @param channel 消息频道
     * @param message 消息内容
     */
    @Override
    public void onMessage(final String channel, final String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                Bukkit.getLogger().warning(LOG_PREFIX + "收到空消息，频道: " + channel);
                return;
            }

            // 记录接收到的消息
            Bukkit.getLogger().info(LOG_PREFIX + "收到消息 - 频道: " + channel + ", 内容: " + message);

            // 检查是否是地图加载消息
            if (channel.equals(MAP_LOAD_CHANNEL_PREFIX + IPUtil.getLocalIp())) {
                // 提交地图加载请求
                mapLoadManager.submitMapLoadRequest(message);
                // 加载地图

                if (mapLoadManager.loadMap(message)) {
                    Bukkit.getLogger().info(LOG_PREFIX + "地图加载成功");
                } else {
                    Bukkit.getLogger().severe(LOG_PREFIX + "地图加载失败");
                }
                return;
            }

            // 触发通用消息事件
            AzuraBedWars.getInstance().callEvent(new BukkitPubSubMessageEvent(channel, message));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, LOG_PREFIX + "处理消息时发生错误", e);
        }
    }
}