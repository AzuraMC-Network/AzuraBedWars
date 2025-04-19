package cc.azuramc.bedwars.jedis.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
@ToString
public class BukkitPubSubMessageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String channel;
    private final String message;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }


}