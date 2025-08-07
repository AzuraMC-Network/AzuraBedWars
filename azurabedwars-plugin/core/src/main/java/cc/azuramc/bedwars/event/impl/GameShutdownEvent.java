package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import cc.azuramc.bedwars.util.BungeeUtil;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 处理游戏结束时需要执行的清理工作和关服操作
 *
 * @author an5w1r@163.com
 */
public class GameShutdownEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventSettingsConfig.GameOverEvent gameOverConfig = PLUGIN.getEventSettingsConfig().getGameOverEvent();

    /**
     * 创建游戏结束事件
     */
    public GameShutdownEvent() {
        super("游戏关闭", gameOverConfig.getExecuteSeconds(), 7);
    }

    /**
     * 执行游戏结束事件
     * 清理资源并触发服务器关闭
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {

        // 所有游戏玩家传送到大厅
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            gameTeam.getGamePlayers().forEach(BungeeUtil::connectToLobby);
        }

        performCleanup();

        shutdownServer();
    }

    /**
     * 执行游戏结束前的清理工作
     */
    private void performCleanup() {
        // 释放强制加载的区块
        ChunkListener.releaseForceLoadedChunks();
    }

    /**
     * 关闭服务器
     */
    private void shutdownServer() {
        Bukkit.shutdown();
    }
}
