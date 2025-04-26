package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsGameOverEvent;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.GameOverTask;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.jedis.event.JedisGameEndEvent;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 负责处理游戏结束时的逻辑，包括显示结束信息和切换到下一个事件
 */
public class AbstractGameOverEvent extends AbstractGameEvent {

    private static final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private static final EventConfig.OverEvent config = plugin.getEventConfig().getOverEvent();
    private static final MessageConfig.OverEvent messageConfig = plugin.getMessageConfig().getOverEvent();

    public AbstractGameOverEvent() {
        super(messageConfig.getEventName(), config.getExecuteSecond(), 6);
    }

    @Override
    public void execute(GameManager gameManager) {
        gameManager.getGameEventManager().setCurrentEvent(7);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(gameManager.getWinner()));
        Bukkit.getPluginManager().callEvent(new JedisGameEndEvent());
        new GameOverTask(gameManager);
    }
}
