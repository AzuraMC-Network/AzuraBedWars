package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.GameStartTask;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.scoreboard.base.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大厅计分板管理类
 * <p>
 * 负责创建、更新和删除玩家在大厅中的计分板
 * </p>
 */
public class LobbyBoardProvider implements Listener {
    // 游戏实例
    private static GameManager gameManager;
    
    // 常量定义
    private static final String TITLE = "§e§l超级起床战争";
    private static final String SERVER_INFO = "§bas.azuramc.cc";
    private static final String WAITING_MESSAGE = "§f等待中...";
    private static final String EMPTY_LINE = "";
    private static final String DEFAULT_MODE = "普通模式";
    private static final String EXP_MODE = "经验模式";
    
    // 玩家计分板更新状态缓存
    private static final ConcurrentHashMap<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    
    // 更新间隔（毫秒）
    private static final long UPDATE_INTERVAL = 500; // 默认为0.5秒更新一次
    
    // 插件实例缓存
    private static AzuraBedWars plugin;

    /**
     * 构造函数
     * 
     * @param gameManager 游戏实例
     */
    public LobbyBoardProvider(GameManager gameManager) {
        LobbyBoardProvider.gameManager = gameManager;
        plugin = AzuraBedWars.getInstance();
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
        
        // 更新玩家等级
        updatePlayerLevel(player, gamePlayer.getPlayerProfile());
        
        List<String> lines = new ArrayList<>();
        
        // 添加地图信息
        addMapInfo(lines);
        
        // 添加玩家信息
        addPlayerInfo(lines);
        
        // 添加倒计时信息
        addCountdownInfo(lines);
        
        // 添加模式信息
        addModeInfo(lines, gamePlayer.getPlayerProfile());
        
        // 添加版本信息
        addVersionInfo(lines);
        
        // 添加服务器信息
        addServerInfo(lines);
        
        // 更新计分板
        board.updateLines(lines.toArray(new String[0]));
    }
    
    /**
     * 更新玩家等级
     * 
     * @param player 玩家
     * @param playerProfile 玩家数据
     */
    private static void updatePlayerLevel(Player player, PlayerProfile playerProfile) {
        int expPoints = (playerProfile.getKills() * 2) +
                       (playerProfile.getDestroyedBeds() * 10) +
                       (playerProfile.getWins() * 15);
        int level = plugin.getLevel(expPoints);
        player.setLevel(level);
    }
    
    /**
     * 添加地图信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addMapInfo(List<String> lines) {
        lines.add(EMPTY_LINE);
        lines.add("§f地图: §a" + gameManager.getMapData().getName());
        lines.add("§f队伍: §a" + gameManager.getMapData().getPlayers().getTeam() + "人 " + gameManager.getGameTeams().size() + "队");
        lines.add("§f作者: §a" + gameManager.getMapData().getAuthor());
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加玩家信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addPlayerInfo(List<String> lines) {
        lines.add("§f玩家: §a" + GamePlayer.getOnlinePlayers().size() + "/" + gameManager.getMaxPlayers());
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加倒计时信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addCountdownInfo(List<String> lines) {
        String countdown = getCountdown();
        if (countdown != null) {
            lines.add(countdown);
        }
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加模式信息到计分板
     * 
     * @param lines 计分板行列表
     * @param playerProfile 玩家数据
     */
    private static void addModeInfo(List<String> lines, PlayerProfile playerProfile) {
        String modeText = playerProfile.getGameModeType() == GameModeType.DEFAULT ? DEFAULT_MODE : EXP_MODE;
        lines.add("§f你的模式: §a" + modeText);
        lines.add(EMPTY_LINE);
    }
    
    /**
     * 添加版本信息到计分板
     * 
     * @param lines 计分板行列表
     */
    private static void addVersionInfo(List<String> lines) {
        lines.add("§f版本: §a" + plugin.getDescription().getVersion());
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
     * 玩家加入事件处理
     * 
     * @param e 玩家加入事件
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // 为新加入的玩家创建计分板
        show(e.getPlayer());
        // 更新所有玩家的计分板
        updateBoard();
    }
    
    /**
     * 玩家退出事件处理
     * 
     * @param e 玩家退出事件
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // 清理玩家的计分板
        removeBoard(e.getPlayer());
        // 更新所有玩家的计分板
        updateBoard();
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
                java.lang.reflect.Field field = LobbyBoardProvider.class.getDeclaredField("UPDATE_INTERVAL");
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
