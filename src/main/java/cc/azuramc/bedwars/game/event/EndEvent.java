package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.events.BedwarsGameEndEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.listeners.ChunkListener;
import org.bukkit.Bukkit;

/**
 * 游戏结束事件
 * 处理游戏结束时需要执行的清理工作和关服操作
 */
public class EndEvent extends GameEvent {
    
    // 游戏结束延迟关服时间（秒）
    private static final int SHUTDOWN_DELAY_SECONDS = 30;
    
    // 事件优先级（在所有事件结束后触发）
    private static final int END_EVENT_PRIORITY = 7;
    
    /**
     * 创建游戏结束事件
     */
    public EndEvent() {
        super("游戏结束！", SHUTDOWN_DELAY_SECONDS, END_EVENT_PRIORITY);
    }

    /**
     * 执行游戏结束事件
     * 清理资源并触发服务器关闭
     *
     * @param game 游戏实例
     */
    @Override
    public void excute(Game game) {

        performCleanup();

        fireGameEndEvent();

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
     * 触发游戏结束事件
     */
    private void fireGameEndEvent() {
        Bukkit.getPluginManager().callEvent(new BedwarsGameEndEvent());
    }
    
    /**
     * 关闭服务器
     */
    private void shutdownServer() {
        Bukkit.shutdown();
    }
}
