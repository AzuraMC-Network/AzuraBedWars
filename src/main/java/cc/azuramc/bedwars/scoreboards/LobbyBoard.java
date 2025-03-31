package cc.azuramc.bedwars.scoreboards;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameLobbyCountdown;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.utils.board.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class LobbyBoard implements Listener {
    private static Game game;

    public LobbyBoard(Game game) {
        LobbyBoard.game = game;
    }

    public static void show(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() == null) {
            FastBoard board = new FastBoard(player);
            board.updateTitle("§e§l超级起床战争");
            gamePlayer.setBoard(board);
            updateBoard();
        }
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            FastBoard board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null || board == null) {
                continue;
            }

            PlayerData playerData = gamePlayer.getPlayerData();
            int level = AzuraBedWars.getInstance().getLevel((playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15));
            player.setLevel(level);

            List<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("§f地图: §a" + game.getMapData().getName());
            lines.add("§f队伍: §a" + game.getMapData().getPlayers().getTeam() + "人 " + game.getGameTeams().size() + "队");
            lines.add("§f作者: §a" + game.getMapData().getAuthor());
            lines.add("");
            lines.add("§f玩家: §a" + GamePlayer.getOnlinePlayers().size() + "/" + game.getMaxPlayers());
            lines.add("");
            String countdown = getCountdown();
            if (countdown != null) {
                lines.add(countdown);
            }
            lines.add("");
            lines.add("§f你的模式: §a" + (playerData.getModeType() == ModeType.DEFAULT ? "普通模式" : "经验模式"));
            lines.add("");
            lines.add("§f版本: §a" + AzuraBedWars.getInstance().getDescription().getVersion());
            lines.add("");
            lines.add("§bas.azuramc.cc");

            board.updateLines(lines.toArray(new String[0]));
        }
    }

    private static String getCountdown() {
        Game game = AzuraBedWars.getInstance().getGame();
        GameLobbyCountdown gameLobbyCountdown = game.getGameLobbyCountdown();

        if (gameLobbyCountdown != null) {
            return gameLobbyCountdown.getCountdown() + "秒后开始";
        } else if (game.getGameState() == GameState.WAITING) {
            return "§f等待中...";
        }

        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        LobbyBoard.updateBoard();
    }

    public static void removeBoard(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() != null) {
            FastBoard board = gamePlayer.getBoard();
            board.delete();
            gamePlayer.setBoard(null);
        }
    }
}
