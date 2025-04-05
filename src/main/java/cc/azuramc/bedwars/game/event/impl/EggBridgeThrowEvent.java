package cc.azuramc.bedwars.game.event.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EggBridgeThrowEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * -- GETTER --
     *  获取目标玩家
     */
    private final Player player;

    @Setter
    private boolean cancelled = false;

    /**
     * 当玩家扔出搭桥蛋触发搭桥时调用
     */
    public EggBridgeThrowEvent(Player player) {
        this.player = player;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
