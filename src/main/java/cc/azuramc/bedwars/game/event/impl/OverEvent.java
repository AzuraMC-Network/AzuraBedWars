package cc.azuramc.bedwars.game.event.impl;

import cc.azuramc.bedwars.events.BedwarsGameOverEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameOverRunnable;
import cc.azuramc.bedwars.game.event.GameEvent;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 负责处理游戏结束时的逻辑，包括显示结束信息和切换到下一个事件
 */
public class OverEvent extends GameEvent {
    public OverEvent() {
        super("游戏结束", 600, 6);
    }

    public void excute(Game game) {
        game.getEventManager().setCurrentEvent(7);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(game.getWinner()));
        new GameOverRunnable(game);
    }
}
