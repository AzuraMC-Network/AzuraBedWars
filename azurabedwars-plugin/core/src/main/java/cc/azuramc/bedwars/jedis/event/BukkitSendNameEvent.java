package cc.azuramc.bedwars.jedis.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 服务器名称设置事件
 * 用于在Bukkit端设置服务器名称
 *
 * @author an5w1r@163.com
 */
@Getter
public class BukkitSendNameEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String serverName;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 构造函数
     *
     * @param serverName 要设置的服务器名称
     */
    public BukkitSendNameEvent(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
