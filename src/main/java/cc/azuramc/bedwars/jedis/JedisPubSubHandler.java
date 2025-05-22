package cc.azuramc.bedwars.jedis;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.jedis.event.BukkitPubSubMessageEvent;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

/**
 * Redis PubSub消息处理器
 * 负责处理从Redis订阅频道接收到的消息，并将消息转发给相应的处理器
 *
 * @author an5w1r@163.com
 */
public class JedisPubSubHandler extends JedisPubSub {
    
    private static final String LOG_PREFIX = "[JedisPubSub] ";

    private final AzuraBedWars plugin;
    
    public JedisPubSubHandler(AzuraBedWars plugin) {
        this.plugin = plugin;
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
                return;
            }

            if ("requestMap".equals(message)) {
                return;
            }

            Bukkit.getLogger().info(LOG_PREFIX + "收到消息 - 频道: " + channel + ", 内容: " + message);
            plugin.callEvent(new BukkitPubSubMessageEvent(channel, message));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, LOG_PREFIX + "处理消息时发生错误", e);
        }
    }
}