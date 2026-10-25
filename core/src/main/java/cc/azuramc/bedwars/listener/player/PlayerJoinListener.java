package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.ReconnectState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerJoinListener implements Listener {

    private static final String ADMIN_PERMISSION = "azurabedwars.admin";

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        // no GamePlayer yet, a refused login never fires PlayerQuitEvent
        if (player.hasPermission(ADMIN_PERMISSION)
                || (gameManager.getGameState() == GameState.RUNNING && gameManager.hasReconnectState(player.getUniqueId()))) {
            event.allow();
            return;
        }

        if (gameManager.getGameState() != GameState.WAITING) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "游戏已开始");
        } else if (Bukkit.getOnlinePlayers().size() >= gameManager.getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "游戏人数已满");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.create(player);

        ReconnectState reconnectState = gameManager.takeReconnectState(player.getUniqueId());
        if (reconnectState != null) {
            reconnectState.restoreTo(gamePlayer, gameManager);
        }
        gameManager.addPlayer(gamePlayer);
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMaxPlayers(gameManager.getMaxPlayers());
    }
}
