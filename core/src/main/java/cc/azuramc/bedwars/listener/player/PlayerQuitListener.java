package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.spectator.SpectatorManager;
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

            if (gamePlayer.isSpectator()) {
                SpectatorManager.remove(gamePlayer);
            }

            // 可重连则先抓断线快照
            if (gameManager.isReconnectable(gamePlayer)) {
                gameManager.saveReconnectState(gamePlayer);
            }

            tabListManager.removePlayerFromTab(gamePlayer);
            gameManager.removePlayers(gamePlayer);

            // 使 GamePlayer 生命周期与在线一致 退出即从在线集合移除
            GamePlayer.remove(gamePlayer.getUuid());
        }
        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
    }
}
