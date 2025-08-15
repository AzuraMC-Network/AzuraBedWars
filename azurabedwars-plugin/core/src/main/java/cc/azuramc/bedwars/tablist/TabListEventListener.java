package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.api.event.bed.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.api.event.game.BedwarsGameEndEvent;
import cc.azuramc.bedwars.api.event.game.BedwarsGameStartEvent;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerKillEvent;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerStateChangeEvent;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * TabList事件监听器
 * 监听BedWars相关事件，实现事件驱动的TabList更新
 *
 * @author an5w1r@163.com
 */
public class TabListEventListener implements Listener {

    private final TabListManager tabListManager;

    public TabListEventListener(TabListManager tabListManager) {
        this.tabListManager = tabListManager;
    }

    @EventHandler
    public void onPlayerStateChange(BedwarsPlayerStateChangeEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        if (gamePlayer != null) {
            tabListManager.updateAllTabListNames();
        }
    }

    @EventHandler
    public void onPlayerKill(BedwarsPlayerKillEvent event) {
        tabListManager.updateAllTabListNames();
        tabListManager.updateHeaderFooter();
    }

    @EventHandler
    public void onBedDestroy(BedwarsDestroyBedEvent event) {
        tabListManager.updateAllTabListNames();
        tabListManager.updateHeaderFooter();
    }

    @EventHandler
    public void onGameStart(BedwarsGameStartEvent event) {
        tabListManager.updateHeaderFooter();
        tabListManager.updateAllTabListNames();
    }

    @EventHandler
    public void onGameEnd(BedwarsGameEndEvent event) {
        tabListManager.updateHeaderFooter();
        tabListManager.updateAllTabListNames();
    }
}
