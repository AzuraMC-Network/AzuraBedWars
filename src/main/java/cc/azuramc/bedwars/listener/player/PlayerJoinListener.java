package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.scoreboard.base.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerJoinListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (gameManager.getGameState() == GameState.RUNNING && GamePlayer.get(player.getUniqueId()).getGameTeam() != null) {
            event.allow();
            return;
        }

        if ((player.hasPermission("azurabedwars.admin"))) {
            event.allow();

            if(gameManager.getGameState() == GameState.RUNNING) {
                return;
            }
        }

        if (GamePlayer.getOnlinePlayers().size() >= gameManager.getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (GamePlayer.get(event.getUniqueId()) != null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.create(event.getUniqueId(), event.getName());
        if (gameManager.getGameState() == GameState.RUNNING) {
            gamePlayer.setSpectator();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            player.kickPlayer("玩家异常状态");
            return;
        }
        gamePlayer.getPlayerProfile().asyncLoadShop();
        FastBoard board = new FastBoard(player);
        board.updateTitle("§e§l起床战争");
        board.updateLines("Test");
        gamePlayer.setBoard(board);
        gameManager.addPlayer(gamePlayer);
    }
}
