package cc.azuramc.bedwars.jedis.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 服务器名称设置事件
 * 用于在Bukkit端设置服务器名称
 */
@Getter
public class BukkitSendNameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String serverName;

    /**
     * 构造函数
     * @param serverName 要设置的服务器名称
     */
    public BukkitSendNameEvent(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}