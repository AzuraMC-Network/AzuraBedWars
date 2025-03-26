package cc.azuramc.bedwars.scoreboards;

import cc.azuramc.bedwars.utils.board.Board;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameLobbyCountdown;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.types.ModeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class LobbyBoard implements Listener {
    private static final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    private static Objective hp;
    private static Objective o;


    private static Game game;

    public LobbyBoard(Game game) {
        LobbyBoard.game = game;
    }

    public static Scoreboard getBoard() {
        return sb;
    }

    public static void show(Player p) {
        if (hp == null) {
            hp = sb.registerNewObjective("NAME_HEALTH", "health");
            hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
            hp.setDisplayName(ChatColor.GOLD + "✫");
        }
        if (o == null) {
            o = sb.registerNewObjective("health", "dummy");
            o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        p.setScoreboard(sb);
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Board board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                continue;
            }
            PlayerData playerData = gamePlayer.getPlayerData();
            hp.getScore(player.getName()).setScore(AzuraBedWars.getInstance().getLevel((playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15)));
            o.getScore(player.getName()).setScore(AzuraBedWars.getInstance().getLevel((playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15)));

            List<String> list = new ArrayList<>();
            list.add(" ");
            list.add("§f地图: §a" + game.getMapData().getName());
            list.add("§f队伍: §a" + game.getMapData().getPlayers().getTeam() + "人 " + game.getGameTeams().size() + "队");
            list.add("§f作者: §a" + game.getMapData().getAuthor());
            list.add("  ");
            list.add("§f玩家: §a" + GamePlayer.getOnlinePlayers().size() + "/" + game.getMaxPlayers());
            list.add("   ");
            list.add(getCountdown());
            list.add("    ");
            list.add("§f你的模式: §a" + (playerData.getModeType() == ModeType.DEFAULT ? "普通模式" : "经验模式"));
            list.add("     ");
            list.add("§f版本: §a" + AzuraBedWars.getInstance().getDescription().getVersion());
            list.add("      ");
            list.add("§bas.azuramc.cc");

            board.send("§e§l超级起床战争", list);
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
}
