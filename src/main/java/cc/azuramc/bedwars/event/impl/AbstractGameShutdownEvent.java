package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsGameEndEvent;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 处理游戏结束时需要执行的清理工作和关服操作
 * @author an5w1r@163.com
 */
public class AbstractGameShutdownEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();

    private static final String EVENT_NAME = PLUGIN.getMessageConfig().getShutdown().getEventName();

    /**
     * 游戏结束延迟关服时间（秒）
     */
    private static final int SHUTDOWN_DELAY_SECONDS = PLUGIN.getEventConfig().getShutdownEvent().getShutdownDelaySecond();

    /**
     * 事件优先级（在所有事件结束后触发）
     */
    private static final int END_EVENT_PRIORITY = 7;
    
    /**
     * 创建游戏结束事件
     */
    public AbstractGameShutdownEvent() {
        super(EVENT_NAME, SHUTDOWN_DELAY_SECONDS, END_EVENT_PRIORITY);
    }

    /**
     * 执行游戏结束事件
     * 清理资源并触发服务器关闭
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {

        // 调用游戏结束事件
        BedwarsGameEndEvent event = new BedwarsGameEndEvent();
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
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
