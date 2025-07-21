package cc.azuramc.bedwars.api.event;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class BedwarsDestroyBedEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final GamePlayer gamePlayer;
    @Getter
    private final GameTeam gameTeam;
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BedwarsDestroyBedEvent(GamePlayer gamePlayer, GameTeam gameTeam) {
        this.gamePlayer = gamePlayer;
        this.gameTeam = gameTeam;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
