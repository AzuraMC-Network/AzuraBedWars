package cc.azuramc.bedwars.scoreboards;

import cc.azuramc.bedwars.events.BedwarsGameStartEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.utils.board.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GameBoard implements Listener {
    private static Game game;

    public GameBoard(Game game) {
        GameBoard.game = game;
    }

    public static void show(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() == null) {
            FastBoard board = new FastBoard(player);
            board.updateTitle("§e§l超级起床战争");
            gamePlayer.setBoard(board);
        }
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            FastBoard board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null || board == null) {
                continue;
            }

            List<String> lines = new ArrayList<>();
            // 添加日期行
            lines.add("§7团队 " + new SimpleDateFormat("MM/dd/yy", Locale.CHINESE).format(Calendar.getInstance().getTime()));
            lines.add("");
            
            // 添加事件信息
            lines.add(game.getEventManager().formattedNextEvent());
            lines.add("§a" + game.getFormattedTime(game.getEventManager().getLeftTime()));
            lines.add("");
            
            // 添加队伍信息
            for (GameTeam gameTeam : game.getGameTeams()) {
                lines.add(gameTeam.getName() + " " + 
                    (gameTeam.isBedDestroy() ? "§7❤" : "§c❤") + 
                    "§f | " + gameTeam.getAlivePlayers().size() + 
                    (gameTeam.isInTeam(gamePlayer) ? " §7(我的队伍)" : ""));
            }
            
            lines.add("");
            lines.add("§bas.azuramc.cc");

            // 更新计分板
            board.updateLines(lines.toArray(new String[0]));
        }
    }

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        // 为所有在线玩家显示计分板
        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }

        // 注册计分板更新任务
        game.getEventManager().registerRunnable("计分板", (s, c) -> updateBoard());
    }

    // 清理玩家的计分板
    public static void removeBoard(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() != null) {
            FastBoard board = gamePlayer.getBoard();
            board.delete();
            gamePlayer.setBoard(null);
        }
    }
}
