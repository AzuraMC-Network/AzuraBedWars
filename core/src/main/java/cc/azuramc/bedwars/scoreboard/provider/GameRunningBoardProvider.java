package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsGameStartEvent;
import cc.azuramc.bedwars.config.object.ScoreboardConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏计分板管理类
 * 负责创建、更新和删除玩家的计分板
 *
 * @author an5w1r@163.com
 */
public class GameRunningBoardProvider implements Listener {

    private static final ScoreboardConfig.GameScoreboard CONFIG = AzuraBedWars.getInstance().getScoreboardConfig().getGameScoreboard();

    private static GameManager gameManager;

    private static final String TITLE = CONFIG.getTitle();
    private static final String SERVER_INFO = CONFIG.getServerInfo();
    private static final String MY_TEAM_MARK = CONFIG.getMyTeamMark();
    private static final String BED_DESTROYED = CONFIG.getBedDestroyed();
    private static final String BED_ALIVE = CONFIG.getBedAlive();
    private static final String SEPARATOR = CONFIG.getSeparator();
    private static final String EMPTY_LINE = CONFIG.getEmptyLine();

    /**
     * 日期格式化器缓存
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy", Locale.CHINESE);

    /**
     * 玩家计分板更新状态缓存
     */
    private static final ConcurrentHashMap<UUID, Long> LAST_UPDATE_TIME = new ConcurrentHashMap<>();

    /**
     * 更新间隔（毫秒）
     */
    private static final long UPDATE_INTERVAL = CONFIG.getUpdateInterval();

    /**
     * 计分板管理器引用
     */
    private static ScoreboardManager scoreboardManager;

    /**
     * 构造函数
     *
     * @param gameManager 游戏实例
     */
    public GameRunningBoardProvider(GameManager gameManager) {
        GameRunningBoardProvider.gameManager = gameManager;
    }

    /**
     * 设置计分板管理器
     *
     * @param manager 计分板管理器
     */
    public static void setScoreboardManager(ScoreboardManager manager) {
        GameRunningBoardProvider.scoreboardManager = manager;
    }

    /**
     * 为玩家创建计分板
     *
     * @param player 玩家
     */
    public static void show(Player player) {
        if (player == null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() == null) {
            FastBoard board = new FastBoard(player);
            board.updateTitle(TITLE);
            gamePlayer.setBoard(board);

            // 立即更新一次
            updatePlayerBoard(gamePlayer);
        }
    }

    /**
     * 更新所有玩家的计分板
     */
    public static void updateBoard() {
        long currentTime = System.currentTimeMillis();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            // 检查更新间隔
            UUID playerId = gamePlayer.getUuid();
            long lastUpdate = LAST_UPDATE_TIME.getOrDefault(playerId, 0L);

            if (currentTime - lastUpdate >= UPDATE_INTERVAL) {
                updatePlayerBoard(gamePlayer);
                LAST_UPDATE_TIME.put(playerId, currentTime);
            }
        }
    }

    /**
     * 更新单个玩家的计分板
     *
     * @param gamePlayer 游戏玩家
     */
    private static void updatePlayerBoard(GamePlayer gamePlayer) {
        FastBoard board = gamePlayer.getBoard();
        Player player = gamePlayer.getPlayer();

        if (player == null || board == null || !player.isOnline()) {
            return;
        }

        List<String> lines = new ArrayList<>();

        // 添加日期行
        lines.add("§7团队 " + DATE_FORMAT.format(Calendar.getInstance().getTime()));
        lines.add(EMPTY_LINE);
        // 添加事件信息
        lines.add(gameManager.getGameEventManager().formattedNextEvent());
        lines.add("§a" + gameManager.getFormattedTime(gameManager.getGameEventManager().getLeftTime()));
        lines.add(EMPTY_LINE);
        // 添加队伍信息
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            StringBuilder teamLine = new StringBuilder()
                    .append(gameTeam.getName())
                    .append(" ")
                    .append(gameTeam.isDestroyed() ? BED_DESTROYED : BED_ALIVE)
                    .append(SEPARATOR)
                    .append(gameTeam.getAlivePlayers().size());

            if (gameTeam.isInTeam(gamePlayer)) {
                teamLine.append(MY_TEAM_MARK);
            }

            lines.add(teamLine.toString());
        }
        lines.add(EMPTY_LINE);
        // 添加服务器信息
        lines.add(SERVER_INFO);

        // 更新计分板
        board.updateLines(lines.toArray(new String[0]));
    }

    /**
     * 游戏开始事件处理
     *
     * @param event 游戏开始事件
     */
    @EventHandler
    public void onStart(BedwarsGameStartEvent event) {
        if (scoreboardManager != null) {
            // 使用计分板管理器切换到游戏模式
            scoreboardManager.switchBoardMode();

            // 注册计分板更新任务
            gameManager.getGameEventManager().registerRunnable("计分板", (s, c) -> {
                if (scoreboardManager != null) {
                    scoreboardManager.updateAllBoards();
                } else {
                    updateBoard();
                }
            });
        } else {
            // 为所有在线玩家显示计分板
            Bukkit.getOnlinePlayers().forEach(GameRunningBoardProvider::show);

            // 注册计分板更新任务
            gameManager.getGameEventManager().registerRunnable("计分板", (s, c) -> updateBoard());
        }
    }

    /**
     * 清理玩家的计分板
     *
     * @param player 玩家
     */
    public static void removeBoard(Player player) {
        if (player == null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() != null) {
            FastBoard board = gamePlayer.getBoard();
            board.delete();
            gamePlayer.setBoard(null);

            // 移除缓存
            LAST_UPDATE_TIME.remove(player.getUniqueId());
        }
    }

    /**
     * 设置更新间隔
     *
     * @param interval 更新间隔（毫秒）
     */
    public static void setUpdateInterval(long interval) {
        if (interval > 0) {
            // 通过反射修改常量值
            try {
                java.lang.reflect.Field field = GameRunningBoardProvider.class.getDeclaredField("UPDATE_INTERVAL");
                field.setAccessible(true);
                java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
                field.set(null, interval);
            } catch (Exception e) {
                // 忽略错误
            }
        }
    }

    /**
     * 清理所有计分板
     */
    public static void removeAllBoards() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null) {
                removeBoard(player);
            }
        }
        LAST_UPDATE_TIME.clear();
    }
}
