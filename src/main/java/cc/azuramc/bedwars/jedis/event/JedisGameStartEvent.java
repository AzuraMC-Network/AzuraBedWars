package cc.azuramc.bedwars.jedis.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏开始事件
 * 当游戏正式开始运行时触发,用于将服务器状态设置为运行状态
 */
public class JedisGameStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    /**
     * 构造函数
     */
    public JedisGameStartEvent() {
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}