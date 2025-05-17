package cc.azuramc.bedwars.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
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

    private final Player player;
    private final Player killer;
    private final boolean last;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BedwarsPlayerKilleEvent(Player player, Player killer, boolean last) {
        this.player = player;
        this.killer = killer;
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
