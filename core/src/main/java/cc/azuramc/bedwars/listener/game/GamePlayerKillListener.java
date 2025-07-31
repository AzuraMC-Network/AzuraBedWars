package cc.azuramc.bedwars.listener.game;

import cc.azuramc.bedwars.tablist.TabList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author An5w1r@163.com
 */
public class GamePlayerKillListener implements Listener {

    @EventHandler
    public void onBedWarsKill() {
        TabList.updateAllTabListNames();
    }
}
