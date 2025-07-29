package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ScoreboardConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import cc.azuramc.bedwars.game.task.GameStartTask;
import cc.azuramc.bedwars.scoreboard.ScoreboardManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大厅计分板管理类
 * 负责创建、更新和删除玩家在大厅中的计分板
 *
 * @author an5w1r@163.com
 */
public class GameEndBoardProvider implements Listener {

    private static final ScoreboardConfig.LobbyScoreboard CONFIG = AzuraBedWars.getInstance().getScoreboardConfig().getLobbyScoreboard();
    private static final String TITLE = CONFIG.getTitle();
    private static final String SERVER_INFO = CONFIG.getServerInfo();
    private static final String WAITING_MESSAGE = CONFIG.getWaitingMessage();
    private static final String EMPTY_LINE = CONFIG.getEmptyLine();
    private static final String DEFAULT_MODE = CONFIG.getDefaultMode();
    private static final String EXP_MODE = CONFIG.getExpMode();

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
    private static GameManager gameManager;
    /**
     * 插件实例缓存
     */
    private static AzuraBedWars plugin;

    /**
     * 计分板管理器引用
     */
    private static ScoreboardManager scoreboardManager;

    /**
     * 构造函数
     *
     * @param gameManager 游戏实例
     */
    public GameEndBoardProvider(GameManager gameManager) {
        GameEndBoardProvider.gameManager = gameManager;
        plugin = AzuraBedWars.getInstance();
    }

    /**
     * 设置计分板管理器
     *
     * @param manager 计分板管理器
     */
    public static void setScoreboardManager(ScoreboardManager manager) {
        GameEndBoardProvider.scoreboardManager = manager;
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

        // 更新玩家等级
        updatePlayerLevel(player, gamePlayer.getPlayerData());

        List<String> lines = new ArrayList<>();

        // 添加日期行
        lines.add("§7团队 " + DATE_FORMAT.format(Calendar.getInstance().getTime()));
        lines.add(EMPTY_LINE);
        // 添加事件信息
        lines.add("&c游戏结束");
        lines.add(EMPTY_LINE);
        // 添加服务器信息
        lines.add(SERVER_INFO);


        // 更新计分板
        board.updateLines(lines.toArray(new String[0]));
    }

    /**
     * 更新玩家等级
     *
     * @param player     玩家
     * @param playerData 玩家数据
     */
    private static void updatePlayerLevel(Player player, PlayerData playerData) {
        // 设置玩家的显示等级
        player.setLevel(playerData.getLevel());

        // 设置经验条显示当前等级的进度
        float progress = (float) PlayerLevelManager.getLevelProgress(playerData);
        player.setExp(progress);
    }

    /**
     * 获取倒计时信息
     *
     * @return 倒计时文本
     */
    private static String getCountdown() {
        GameManager currentGameManager = plugin.getGameManager();
        GameStartTask gameStartTask = currentGameManager.getGameStartTask();

        if (gameStartTask != null) {
            return gameStartTask.getCountdown() + "秒后开始";
        } else if (currentGameManager.getGameState() == GameState.WAITING) {
            return WAITING_MESSAGE;
        }

        return null;
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
                java.lang.reflect.Field field = GameEndBoardProvider.class.getDeclaredField("UPDATE_INTERVAL");
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

    /**
     * 玩家加入事件处理
     *
     * @param e 玩家加入事件
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (scoreboardManager != null) {
            // 使用计分板管理器显示对应计分板
            scoreboardManager.showBoard(GamePlayer.get(e.getPlayer()));
            scoreboardManager.updateAllBoards();
        } else {
            // 为新加入的玩家创建计分板
            show(e.getPlayer());
            // 更新所有玩家的计分板
            updateBoard();
        }
    }

    /**
     * 玩家退出事件处理
     *
     * @param e 玩家退出事件
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (scoreboardManager != null) {
            // 使用计分板管理器移除计分板
            scoreboardManager.removeBoard(GamePlayer.get(e.getPlayer()));
            scoreboardManager.updateAllBoards();
        } else {
            // 清理玩家的计分板
            removeBoard(e.getPlayer());
            // 更新所有玩家的计分板
            updateBoard();
        }
    }
}
