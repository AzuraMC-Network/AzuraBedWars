package cc.azuramc.bedwars.events;

import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsGameOverEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final GameTeam winner;
    private boolean cancelled = false;

    public BedwarsGameOverEvent(GameTeam winner) {
        this.winner = winner;
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
