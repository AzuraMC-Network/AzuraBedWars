package cc.azuramc.bedwars.api.event.player;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家击杀事件
 *
 * @author an5w1r@163.com
 */
@Getter
public class BedwarsPlayerKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 被击杀的玩家
     */
    @NotNull
    private final IGamePlayer gamePlayer;

    /**
     * 击杀者
     */
    @NotNull
    private final IGamePlayer gameKiller;

    /**
     * 是否为最终击杀（被击杀者所在队伍已无床）
     */
    private final boolean last;

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BedwarsPlayerKillEvent(@NotNull IGamePlayer gamePlayer, @NotNull IGamePlayer gameKiller, boolean last) {
        this.gamePlayer = gamePlayer;
        this.gameKiller = gameKiller;
        this.last = last;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
