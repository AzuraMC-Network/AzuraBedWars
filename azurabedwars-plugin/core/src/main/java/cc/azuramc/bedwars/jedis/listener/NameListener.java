package cc.azuramc.bedwars.jedis.listener;

import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.event.BukkitSendNameEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 名称监听器
 * 用于处理服务器名称相关的事件
 *
 * @author an5w1r@163.com
 */
public class NameListener implements Listener {

    /**
     * 处理服务器名称设置事件
     * 当收到BukkitSendNameEvent事件时,更新JedisManager中的服务器名称
     *
     * @param event 服务器名称设置事件
     */
    @EventHandler
    public void onName(BukkitSendNameEvent event) {
        JedisManager.getInstance().getServerData().setName(event.getServerName());
    }
}
