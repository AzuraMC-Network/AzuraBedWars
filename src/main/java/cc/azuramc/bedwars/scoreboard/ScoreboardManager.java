package cc.azuramc.bedwars.scoreboard;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.scoreboard.provider.GameBoardProvider;
import cc.azuramc.bedwars.scoreboard.provider.LobbyBoardProvider;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * 计分板管理器
 * <p>
 * 统一管理游戏内和大厅的计分板提供者
 * </p>
 */
public class ScoreboardManager implements Listener {
    private final GameManager gameManager;
    @Getter GameBoardProvider gameBoardProvider;
    @Getter LobbyBoardProvider lobbyBoardProvider;
    
    /**
     * 构造函数
     * 
     * @param gameManager 游戏管理器实例
     */
    public ScoreboardManager(GameManager gameManager) {
        this.gameManager = gameManager;
        
        // 初始化提供者
        this.gameBoardProvider = new GameBoardProvider(gameManager);
        this.lobbyBoardProvider = new LobbyBoardProvider(gameManager);
        
        // 设置提供者的管理器引用
        GameBoardProvider.setScoreboardManager(this);
        LobbyBoardProvider.setScoreboardManager(this);
    }
    
    /**
     * 根据游戏状态显示对应的计分板
     * 
     * @param player 玩家
     */
    public void showBoard(Player player) {
        if (player == null) return;
        
        switch (gameManager.getGameState()) {
            case RUNNING:
                GameBoardProvider.show(player);
                break;
            case WAITING:
            default:
                LobbyBoardProvider.show(player);
                break;
        }
    }
    
    /**
     * 更新所有玩家的计分板
     */
    public void updateAllBoards() {
        switch (gameManager.getGameState()) {
            case RUNNING:
                GameBoardProvider.updateBoard();
                break;
            case WAITING:
            default:
                LobbyBoardProvider.updateBoard();
                break;
        }
    }
    
    /**
     * 移除玩家的计分板
     * 
     * @param player 玩家
     */
    public void removeBoard(Player player) {
        if (player == null) return;
        
        // 同时移除两种计分板，确保清理完全
        GameBoardProvider.removeBoard(player);
        LobbyBoardProvider.removeBoard(player);
    }
    
    /**
     * 移除所有玩家的计分板
     */
    public void removeAllBoards() {
        GameBoardProvider.removeAllBoards();
        LobbyBoardProvider.removeAllBoards();
    }
    
    /**
     * 切换计分板模式
     * 从大厅模式切换到游戏模式，或反之
     */
    public void switchBoardMode() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null) {
                // 先移除所有计分板
                removeBoard(player);
                // 然后显示对应状态的计分板
                showBoard(player);
            }
        }
    }
    
    /**
     * 设置计分板更新间隔
     * 
     * @param interval 更新间隔（毫秒）
     */
    public void setUpdateInterval(long interval) {
        if (interval > 0) {
            GameBoardProvider.setUpdateInterval(interval);
            LobbyBoardProvider.setUpdateInterval(interval);
        }
    }
    
    /**
     * 初始化计分板系统
     * 注册监听器
     * 
     * @param plugin 插件实例
     */
    public void initialize(org.bukkit.plugin.Plugin plugin) {
        // 注册监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(gameBoardProvider, plugin);
        plugin.getServer().getPluginManager().registerEvents(lobbyBoardProvider, plugin);
    }

}
