package cc.azuramc.bedwars.api.event;

import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@Getter
public class BedwarsPlayerKilleEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GamePlayer gamePlayer;
    private final GamePlayer gameKiller;
    private final boolean last;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BedwarsPlayerKilleEvent(GamePlayer gamePlayer, GamePlayer gameKiller, boolean last) {
        this.gamePlayer = gamePlayer;
        this.gameKiller = gameKiller;
        this.last = last;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
