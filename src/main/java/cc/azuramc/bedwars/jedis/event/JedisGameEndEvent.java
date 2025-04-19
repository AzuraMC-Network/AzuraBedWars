package cc.azuramc.bedwars.jedis.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏结束事件
 * 当游戏结束时触发,用于将服务器状态设置为结束状态
 */
public class JedisGameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public JedisGameEndEvent() {
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}