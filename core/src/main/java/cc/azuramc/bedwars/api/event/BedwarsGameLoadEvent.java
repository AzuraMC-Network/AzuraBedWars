package cc.azuramc.bedwars.api.event;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class BedwarsGameLoadEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    @Getter
    private int maxPlayers;

    public BedwarsGameLoadEvent(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
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
