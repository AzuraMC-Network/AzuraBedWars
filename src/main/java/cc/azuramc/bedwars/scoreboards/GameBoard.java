package cc.azuramc.bedwars.scoreboards;

import cc.azuramc.bedwars.utils.board.Board;
import cc.azuramc.bedwars.events.BedwarsGameStartEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GameBoard implements Listener {
    private static final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    private static Objective hp;
    private static Objective o;

    private static Game game;

    public GameBoard(Game game) {
        GameBoard.game = game;
    }

    public static Scoreboard getBoard() {
        return sb;
    }

    public static void show(Player p) {
        p.setScoreboard(sb);
    }

    public static void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Board board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                continue;
            }
            hp.getScore(player.getName()).setScore((int) player.getHealth());
            o.getScore(player.getName()).setScore((int) player.getHealth());


            List<String> list = new ArrayList<>();
            list.add("§7团队 " + new SimpleDateFormat("MM", Locale.CHINESE).format(Calendar.getInstance().getTime()) + "/" + new SimpleDateFormat("dd", Locale.CHINESE).format(Calendar.getInstance().getTime()) + "/" + new SimpleDateFormat("yy", Locale.CHINESE).format(Calendar.getInstance().getTime()) + " ");
            list.add(" ");
            list.add(game.getEventManager().formattedNextEvent());
            list.add("§a" + game.getFormattedTime(game.getEventManager().getLeftTime()));
            list.add("  ");
            for (GameTeam gameTeam : game.getGameTeams()) {
                list.add(gameTeam.getName() + " " + (gameTeam.isBedDestroy() ? "§7❤" : "§c❤") + "§f | " + (gameTeam.getAlivePlayers().size()) + " " + (gameTeam.isInTeam(gamePlayer) ? " §7(我的队伍)" : ""));
            }
            list.add("   ");
            list.add("§bas.azuramc.cc");

            board.send("§e§l超级起床战争", list);
        }
    }

    /**
     * 兼容全版本的注册计分板目标方法
     * @param name 目标名称
     * @param criteria 标准
     * @return 计分板目标
     */
    private Objective registerNewObjective(String name, String criteria) {
        try {
            // 1.13+版本
            return sb.registerNewObjective(name, criteria, name);
        } catch (NoSuchMethodError e) {
            try {
                // 1.8-1.12版本
                return sb.registerNewObjective(name, criteria);
            } catch (Exception ex) {
                // 如果都失败了，尝试使用反射
                try {
                    return (Objective) sb.getClass().getMethod("registerNewObjective", String.class, String.class)
                            .invoke(sb, name, criteria);
                } catch (Exception exc) {
                    Bukkit.getLogger().warning("无法注册计分板目标: " + name);
                    return null;
                }
            }
        }
    }

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        if (hp == null) {
            hp = registerNewObjective("NAME_HEALTH", "health");
            if (hp != null) {
                hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
                hp.setDisplayName(ChatColor.RED + "❤");
            }
        }
        if (o == null) {
            o = registerNewObjective("health", "dummy");
            if (o != null) {
                o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }
        }

        Util.setPlayerTeamTab();

        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }

        game.getEventManager().registerRunnable("计分板", (s, c) -> updateBoard());
    }
}
