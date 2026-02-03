package cc.azuramc.bedwars.scoreboard;

import cc.azuramc.bedwars.api.event.game.BedwarsGameStartEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.scoreboard.provider.AbstractBoardProvider;
import cc.azuramc.bedwars.scoreboard.provider.GameEndBoardProvider;
import cc.azuramc.bedwars.scoreboard.provider.GameRunningBoardProvider;
import cc.azuramc.bedwars.scoreboard.provider.LobbyBoardProvider;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * 计分板管理器
 * 统一管理游戏内和大厅的计分板提供者
 *
 * @author an5w1r@163.com
 */
@Getter
public class ScoreboardManager implements Listener {

    private final GameManager gameManager;
    private final GameEndBoardProvider gameEndBoardProvider;
    private final GameRunningBoardProvider gameRunningBoardProvider;
    private final LobbyBoardProvider lobbyBoardProvider;

    public ScoreboardManager(GameManager gameManager) {
        this.gameManager = gameManager;

        // 初始化提供者
        this.gameEndBoardProvider = new GameEndBoardProvider(gameManager);
        this.gameRunningBoardProvider = new GameRunningBoardProvider(gameManager);
        this.lobbyBoardProvider = new LobbyBoardProvider(gameManager);
    }

    /**
     * 初始化计分板系统
     * 注册监听器
     *
     * @param plugin 插件实例
     */
    public void initialize(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 获取当前状态对应的计分板提供者
     *
     * @return 当前状态的计分板提供者
     */
    private AbstractBoardProvider getCurrentProvider() {
        return switch (gameManager.getGameState()) {
            case RUNNING -> gameRunningBoardProvider;
            case ENDING -> gameEndBoardProvider;
            default -> lobbyBoardProvider;
        };
    }

    /**
     * 根据游戏状态显示对应的计分板
     *
     * @param gamePlayer 游戏玩家
     */
    public void showBoard(GamePlayer gamePlayer) {
        if (gamePlayer == null || gamePlayer.getPlayer() == null) {
            return;
        }
        getCurrentProvider().show(gamePlayer.getPlayer());
    }

    /**
     * 更新所有玩家的计分板
     */
    public void updateAllBoards() {
        getCurrentProvider().updateBoard();
    }

    /**
     * 移除玩家的计分板
     *
     * @param gamePlayer 游戏玩家
     */
    public void removeBoard(GamePlayer gamePlayer) {
        if (gamePlayer == null || gamePlayer.getPlayer() == null) {
            return;
        }

        Player player = gamePlayer.getPlayer();
        // 移除所有提供者的计分板
        gameEndBoardProvider.removeBoard(player);
        gameRunningBoardProvider.removeBoard(player);
        lobbyBoardProvider.removeBoard(player);
    }

    /**
     * 移除所有玩家的计分板
     */
    public void removeAllBoards() {
        gameEndBoardProvider.removeAllBoards();
        gameRunningBoardProvider.removeAllBoards();
        lobbyBoardProvider.removeAllBoards();
    }

    /**
     * 切换计分板模式
     * 从大厅模式切换到游戏模式，或反之
     */
    public void switchBoardMode() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer != null) {
                // 先移除所有计分板
                removeBoard(gamePlayer);
                // 然后显示对应状态的计分板
                showBoard(gamePlayer);
            }
        }
    }

    /**
     * 玩家加入事件处理
     *
     * @param event 玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer());
        if (gamePlayer != null) {
            showBoard(gamePlayer);
            updateAllBoards();
        }
    }

    /**
     * 玩家退出事件处理
     *
     * @param event 玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer());
        if (gamePlayer != null) {
            removeBoard(gamePlayer);
            updateAllBoards();
        }
    }

    /**
     * 游戏开始事件处理
     *
     * @param event 游戏开始事件
     */
    @EventHandler
    public void onGameStart(BedwarsGameStartEvent event) {
        switchBoardMode();

        Bukkit.getOnlinePlayers().forEach(player -> {
            GamePlayer gamePlayer = GamePlayer.get(player);
            if (gamePlayer != null) {
                showBoard(gamePlayer);
            }
        });

        gameManager.getGameEventManager().registerRunnable("计分板", (s, c) -> updateAllBoards());
    }
}
