package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class BedwarsPlayerReconnectEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 重连的玩家
     */
    private final GamePlayer gamePlayer;

    /**
     * 玩家所属的队伍
     */
    private final GameTeam gameTeam;

    /**
     * 游戏管理器
     */
    private final GameManager gameManager;

    /**
     * 事件是否被取消
     */
    private boolean cancelled = false;

    /**
     * 构造函数
     *
     * @param gamePlayer  重连的玩家
     * @param gameTeam    玩家所属队伍
     * @param gameManager 游戏管理器
     */
    public BedwarsPlayerReconnectEvent(@NotNull GamePlayer gamePlayer, @NotNull GameTeam gameTeam, @NotNull GameManager gameManager) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
        this.gameManager = gameManager;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
