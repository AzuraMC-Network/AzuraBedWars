package cc.azuramc.bedwars.jedis.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏加载事件
 * 当游戏开始加载时触发,用于设置服务器状态和最大玩家数量
 *
 * @author an5w1r@163.com
 */
@Getter
public class JedisGameLoadingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int maxPlayers;

    /**
     * 构造函数
     * @param maxPlayers 游戏最大玩家数量
     */
    public JedisGameLoadingEvent(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}