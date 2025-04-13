package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.api.event.BedwarsGameOverEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.GameLoopTask;
import cc.azuramc.bedwars.event.GameEvent;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 负责处理游戏结束时的逻辑，包括显示结束信息和切换到下一个事件
 */
public class GameOverEvent extends GameEvent {
    public GameOverEvent() {
        super("游戏结束", 600, 6);
    }

    public void execute(GameManager gameManager) {
        gameManager.getGameEventManager().setCurrentEvent(7);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(gameManager.getWinner()));
        new GameLoopTask(gameManager);
    }
}
