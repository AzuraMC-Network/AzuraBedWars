package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsGameOverEvent;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.GameOverTask;
import cc.azuramc.bedwars.jedis.event.JedisGameEndEvent;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 负责处理游戏结束时的逻辑，包括显示结束信息和切换到下一个事件
 * @author an5w1r@163.com
 */
public class GameOverEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventConfig.OverEvent CONFIG = PLUGIN.getEventConfig().getOverEvent();
    private static final MessageConfig.Over MESSAGE_CONFIG = PLUGIN.getMessageConfig().getOver();

    public GameOverEvent() {
        super(MESSAGE_CONFIG.getEventName(), CONFIG.getExecuteSecond(), 6);
    }

    @Override
    public void execute(GameManager gameManager) {
        BedwarsGameOverEvent bedwarsGameOverEvent = new BedwarsGameOverEvent(gameManager.getWinner());
        Bukkit.getPluginManager().callEvent(bedwarsGameOverEvent);
        if (bedwarsGameOverEvent.isCancelled()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new JedisGameEndEvent());

        gameManager.getGameEventManager().setCurrentEvent(7);
        new GameOverTask(gameManager);
    }
}
