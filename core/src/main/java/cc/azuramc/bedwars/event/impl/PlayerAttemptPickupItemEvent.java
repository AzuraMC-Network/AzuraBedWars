package cc.azuramc.bedwars.event.impl;

import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author ant1aura@qq.com
 */
public class PlayerAttemptPickupItemEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter private final Player player;
    @Getter private final Item item;

    public PlayerAttemptPickupItemEvent(Player player, Item item) {
        this.player = player;
        this.item = item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
