package cc.azuramc.bedwars.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class BedwarsPlayerKilledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Player killer;
    private final boolean last;

    public BedwarsPlayerKilledEvent(Player player, Player killer, boolean last) {
        this.player = player;
        this.killer = killer;
        this.last = last;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
