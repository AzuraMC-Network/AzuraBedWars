package cc.azuramc.bedwars.game.event.impl;

import cc.azuramc.bedwars.game.TeamColor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EggBridgeBuildEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * -- GETTER --
     *  获取方块的队伍颜色
     */
    private final TeamColor teamColor;

    /**
     * -- GETTER --
     *  获取建造的方块
     */
    private final Block block;

    /**
     * 当搭桥蛋开始生成方块时调用
     */
    public EggBridgeBuildEvent(TeamColor teamColor, Block block) {
        this.teamColor = teamColor;
        this.block = block;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
