package cc.azuramc.bedwars.jedis.listener;

import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.data.ServerType;
import cc.azuramc.bedwars.jedis.event.JedisGameLoadingEvent;
import cc.azuramc.bedwars.jedis.event.JedisGameEndEvent;
import cc.azuramc.bedwars.jedis.event.JedisGameStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 服务器状态监听器
 * 用于处理游戏不同阶段的状态转换事件
 *
 * @author an5w1r@163.com
 */
public class ServerListener implements Listener {

    /**
     * 处理游戏加载事件
     * 当收到JedisGameLoadingEvent事件时,将服务器状态设置为等待状态
     * 并设置最大玩家数量
     *
     * @param event 游戏加载事件
     */
    @EventHandler
    public void onGameLoading(JedisGameLoadingEvent event) {
        JedisManager.getInstance().getServerData().setServerType(ServerType.WAITING);
        JedisManager.getInstance().getServerData().setMaxPlayers(event.getMaxPlayers());
    }

    /**
     * 处理游戏开始事件
     * 当收到JedisGameStartEvent事件时,将服务器状态设置为运行状态
     *
     * @param event 游戏开始事件
     */
    @EventHandler
    public void onGameStart(JedisGameStartEvent event) {
        JedisManager.getInstance().getServerData().setServerType(ServerType.RUNNING);
    }

    /**
     * 处理游戏结束事件
     * 当收到JedisGameEndEvent事件时,将服务器状态设置为结束状态
     *
     * @param event 游戏结束事件
     */
    @EventHandler
    public void onGameEnd(JedisGameEndEvent event) {
        JedisManager.getInstance().getServerData().setServerType(ServerType.END);
    }
}
