package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.utils.board.Board;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Collections;

public class JoinListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (game.getGameState() == GameState.RUNNING && GamePlayer.get(player.getUniqueId()).getGameTeam() != null) {
            event.allow();
            return;
        }

        if ((player.hasPermission("bw.*"))) {
            event.allow();

            if(game.getGameState() == GameState.RUNNING) {
                return;
            }
        }

        if (GamePlayer.getOnlinePlayers().size() >= game.getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "开始了");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (GamePlayer.get(event.getUniqueId()) != null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.create(event.getUniqueId(), event.getName());
        if (game.getGameState() == GameState.RUNNING) {
            gamePlayer.setSpectator();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            player.kickPlayer("进你个锤子");
            return;
        }
        gamePlayer.getPlayerData().asyncLoadShop();
        gamePlayer.setBoard(new Board(player, "SB", Collections.singletonList("Test")));
        game.addPlayer(gamePlayer);
    }
}
