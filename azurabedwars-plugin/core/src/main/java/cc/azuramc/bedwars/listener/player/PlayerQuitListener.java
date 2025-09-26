package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.tablist.TabListManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerQuitListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private final TabListManager tabListManager = gameManager.getTabListManager();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer());
        if (gamePlayer != null) {
            gamePlayer.endInvisibility();

            tabListManager.removePlayerFromTab(gamePlayer);
            gameManager.removePlayers(gamePlayer);
        }
        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
    }
}
