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
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * 游戏计分板管理类
 * <p>
 * 负责创建、更新和删除玩家的计分板
 * </p>
 */
public class GameBoard implements Listener {
    // 游戏实例
    private static Game game;
    
    // 常量定义
    private static final String TITLE = "§e§l超级起床战争";
    private static final String SERVER_INFO = "§bas.azuramc.cc";
    private static final String MY_TEAM_MARK = " §7(我的队伍)";
    private static final String BED_DESTROYED = "§7❤";
    private static final String BED_ALIVE = "§c❤";
    private static final String SEPARATOR = "§f | ";
    private static final String EMPTY_LINE = "";
    
    // 日期格式化器缓存
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy", Locale.CHINESE);
    
    // 玩家计分板更新状态缓存
    private static final ConcurrentHashMap<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    
    // 更新间隔（毫秒）
    private static final long UPDATE_INTERVAL = 500; // 默认为0.5秒更新一次

    /**
     * 构造函数
     * 
     * @param game 游戏实例
     */
    public GameBoard(Game game) {
        GameBoard.game = game;
    }

    /**
     * 为玩家创建计分板
     * 
     * @param player 玩家
     */
    public static void show(Player player) {
        if (player == null) return;
        
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
            long lastUpdate = lastUpdateTime.getOrDefault(playerId, 0L);
            
            if (currentTime - lastUpdate >= UPDATE_INTERVAL) {
                updatePlayerBoard(gamePlayer);
                lastUpdateTime.put(playerId, currentTime);
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
        addDateInfo(lines);
        
        // 添加事件信息
        addEventInfo(lines);
        
        // 添加队伍信息
        addTeamsInfo(lines, gamePlayer);
        
        // 添加服务器信息
        addServerInfo(lines);
        
        // 更新计分板
        board.updateLines(lines.toArray(new String[0]));
    }
    
    /**
     * 添加日期信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addDateInfo(List<String> lines) {
        lines.add("§7团队 " + DATE_FORMAT.format(Calendar.getInstance().getTime()));
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加事件信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addEventInfo(List<String> lines) {
        lines.add(game.getEventManager().formattedNextEvent());
        lines.add("§a" + game.getFormattedTime(game.getEventManager().getLeftTime()));
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加队伍信息到计分板
     * 
     * @param lines 计分板行列表
     * @param gamePlayer 游戏玩家
     */
    private static void addTeamsInfo(List<String> lines, GamePlayer gamePlayer) {
        for (GameTeam gameTeam : game.getGameTeams()) {
            StringBuilder teamLine = new StringBuilder()
                .append(gameTeam.getName())
                .append(" ")
                .append(gameTeam.isBedDestroy() ? BED_DESTROYED : BED_ALIVE)
                .append(SEPARATOR)
                .append(gameTeam.getAlivePlayers().size());
            
            if (gameTeam.isInTeam(gamePlayer)) {
                teamLine.append(MY_TEAM_MARK);
            }
            
            lines.add(teamLine.toString());
        }
        
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加服务器信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addServerInfo(List<String> lines) {
        lines.add(SERVER_INFO);
    }

    /**
     * 游戏开始事件处理
     * 
     * @param e 游戏开始事件
     */
    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        // 为所有在线玩家显示计分板
        Bukkit.getOnlinePlayers().forEach(GameBoard::show);

        // 注册计分板更新任务
        game.getEventManager().registerRunnable("计分板", (s, c) -> updateBoard());
    }

    /**
     * 清理玩家的计分板
     * 
     * @param player 玩家
     */
    public static void removeBoard(Player player) {
        if (player == null) return;
        
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() != null) {
            FastBoard board = gamePlayer.getBoard();
            board.delete();
            gamePlayer.setBoard(null);
            
            // 移除缓存
            lastUpdateTime.remove(player.getUniqueId());
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
                java.lang.reflect.Field field = GameBoard.class.getDeclaredField("UPDATE_INTERVAL");
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
        lastUpdateTime.clear();
    }
}
