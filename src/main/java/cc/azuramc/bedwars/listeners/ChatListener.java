package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * 聊天监听器类
 * <p>
 * 处理游戏中玩家聊天消息的格式化与发送
 * 支持团队聊天和全局聊天
 * 包含玩家等级显示和权限控制
 * </p>
 */
public class ChatListener implements Listener {
    // 常量定义
    private static final String GLOBAL_CHAT_PREFIX = "!";
    private static final String SPECTATOR_PREFIX = "§7[旁观者]";
    private static final String GLOBAL_CHAT_TAG = "§6[全局]";
    private static final String TEAM_CHAT_TAG = "§9[团队]";
    private static final String CHAT_SEPARATOR = "§7: ";
    
    private final Game game;
    private final AzuraBedWars plugin;

    /**
     * 构造方法
     */
    public ChatListener() {
        this.plugin = AzuraBedWars.getInstance();
        this.game = plugin.getGame();
    }

    /**
     * 处理玩家聊天事件
     * 
     * @param event 异步玩家聊天事件
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        
        // 构建基础聊天消息
        String chatMessage = null;
        if (gamePlayer != null) {
            chatMessage = buildChatMessage(player, gamePlayer, message);
        }

        // 根据游戏状态发送消息
        if (game.getGameState() == GameState.RUNNING && !game.getEventManager().isOver() && gamePlayer != null) {
            handleInGameChat(player, gamePlayer, chatMessage);
        } else {
            // 游戏未开始或已结束时的聊天
            game.broadcastMessage(chatMessage);
        }
    }

    /**
     * 构建聊天消息
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param message 原始消息
     * @return 格式化后的聊天消息
     */
    private String buildChatMessage(Player player, GamePlayer gamePlayer, String message) {
        PlayerData playerData = gamePlayer.getPlayerData();
        int level = calculatePlayerLevel(playerData);
        String globalPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getChat().getPlayerPrefix(player));
        
        return "§6[" + plugin.getLevel(level) + "✫]" + globalPrefix + "§7" + gamePlayer.getNickName() + CHAT_SEPARATOR + message;
    }

    /**
     * 计算玩家等级
     * 
     * @param playerData 玩家数据
     * @return 玩家等级
     */
    private int calculatePlayerLevel(PlayerData playerData) {
        return (playerData.getKills() * 2) + 
               (playerData.getDestroyedBeds() * 10) + 
               (playerData.getWins() * 15);
    }

    /**
     * 处理游戏中的聊天消息
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param message 消息内容
     */
    private void handleInGameChat(Player player, GamePlayer gamePlayer, String message) {
        if (gamePlayer.isSpectator()) {
            handleSpectatorChat(player, gamePlayer, message);
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        boolean isGlobalChat = message.startsWith(GLOBAL_CHAT_PREFIX);
        
        // 构建团队聊天消息
        String formattedMessage = buildTeamChatMessage(gamePlayer, gameTeam, message, isGlobalChat);
        
        // 发送消息
        if (isGlobalChat) {
            game.broadcastMessage(formattedMessage);
        } else {
            game.broadcastTeamMessage(gameTeam, formattedMessage);
        }
    }

    /**
     * 处理观察者聊天
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param message 消息内容
     */
    private void handleSpectatorChat(Player player, GamePlayer gamePlayer, String message) {
        String spectatorMessage = SPECTATOR_PREFIX + "§f" + gamePlayer.getNickName() + CHAT_SEPARATOR + message;
        
        if (player.hasPermission("bw.*")) {
            game.broadcastMessage(spectatorMessage);
        } else {
            game.broadcastSpectatorMessage(spectatorMessage);
        }
    }

    /**
     * 构建团队聊天消息
     * 
     * @param gamePlayer 游戏玩家对象
     * @param gameTeam 玩家所在团队
     * @param message 原始消息
     * @param isGlobalChat 是否为全局聊天
     * @return 格式化后的团队聊天消息
     */
    private String buildTeamChatMessage(GamePlayer gamePlayer, GameTeam gameTeam, String message, boolean isGlobalChat) {

        return (isGlobalChat ? GLOBAL_CHAT_TAG : TEAM_CHAT_TAG) +
                gameTeam.getChatColor() +
                "[" +
                gameTeam.getName() +
                "]" +
                gamePlayer.getNickName() +
                CHAT_SEPARATOR +
                (isGlobalChat ? message.substring(1) : message);
    }
}
