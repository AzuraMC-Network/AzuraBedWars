package cc.azuramc.bedwars.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerKilledEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Player killer;
    @Getter
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
    public HandlerList getHandlers() {
        return handlers;
    }
}
