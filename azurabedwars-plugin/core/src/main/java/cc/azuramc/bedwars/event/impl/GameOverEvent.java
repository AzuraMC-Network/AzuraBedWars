package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameOverEvent;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.GameOverTask;
import cc.azuramc.bedwars.jedis.event.JedisGameEndEvent;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 负责处理游戏结束时的逻辑，包括显示结束信息和切换到下一个事件
 *
 * @author an5w1r@163.com
 */
public class GameOverEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventSettingsConfig.GameOverEvent gameOverConfig = PLUGIN.getEventSettingsConfig().getGameOverEvent();

    public GameOverEvent() {
        super("游戏结束", gameOverConfig.getExecuteSeconds(), 6);
    }

    @Override
    public void execute(GameManager gameManager) {
        BedwarsGameOverEvent bedwarsGameOverEvent = new BedwarsGameOverEvent(gameManager);
        Bukkit.getPluginManager().callEvent(bedwarsGameOverEvent);

        Bukkit.getPluginManager().callEvent(new JedisGameEndEvent());

        gameManager.getGameEventManager().setCurrentEvent(7);
        new GameOverTask(gameManager);
    }
}
