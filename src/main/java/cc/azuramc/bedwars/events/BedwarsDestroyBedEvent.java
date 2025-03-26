package cc.azuramc.bedwars.events;

import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsDestroyBedEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final GameTeam gameTeam;
    private boolean cancelled = false;

    public BedwarsDestroyBedEvent(Player player, GameTeam gameTeam) {
        this.player = player;
        this.gameTeam = gameTeam;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
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
