package cc.azuramc.bedwars.listener.chat;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ChatConfig;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Bukkit;
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
 * @author an5w1r@163.com
 */
public class ChatListener implements Listener {

    private static final ChatConfig CONFIG = AzuraBedWars.getInstance().getChatConfig();

    // 常量定义
    public static final String GLOBAL_CHAT_PREFIX = CONFIG.getGlobalChatPrefix();
    private static final String SPECTATOR_PREFIX = CONFIG.getSpectatorPrefix();
    private static final String GLOBAL_CHAT_TAG = CONFIG.getGlobalChatTag();
    private static final String TEAM_CHAT_TAG = CONFIG.getTeamChatTag();
    private static final String CHAT_SEPARATOR = CONFIG.getChatSeparator();
    private static final int GLOBAL_CHAT_COOLDOWN = CONFIG.getGlobalChatCooldown();

    private static GameManager gameManager;
    private static AzuraBedWars plugin;

    /**
     * 构造方法
     */
    public ChatListener() {
        plugin = AzuraBedWars.getInstance();
        gameManager = plugin.getGameManager();
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
            chatMessage = buildChatMessage(gamePlayer, message);
        }

        // 根据游戏状态发送消息
        if (gameManager.getGameState() == GameState.RUNNING && !gameManager.getGameEventManager().isOver() && gamePlayer != null) {
            handleInGameChat(gamePlayer, message);
        } else {
            // 游戏未开始或已结束时的聊天，全服可见
            gameManager.broadcastMessage(chatMessage);
        }

        // 记录冷却
        if (gameManager.getGameState() == GameState.RUNNING && gamePlayer != null) {
            checkShoutCooldown(gamePlayer);
        }
    }

    /**
     * 构建聊天消息
     *
     * @param gamePlayer 游戏玩家
     */
    public static void checkShoutCooldown(GamePlayer gamePlayer) {
        // 玩家不为空且游戏未运行
        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        // 有权限则绕过检查
        if (gamePlayer.getPlayer().hasPermission("azurabedwars.admin")) {
            return;
        }

        // 启动倒计时
        gamePlayer.setShoutCooldown(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> gamePlayer.setShoutCooldown(false), GLOBAL_CHAT_COOLDOWN * 20L);
    }

    /**
     * 构建聊天消息
     *
     * @param gamePlayer 游戏玩家对象
     * @param message 原始消息
     * @return 格式化后的聊天消息
     */
    public static String buildChatMessage(GamePlayer gamePlayer, String message) {
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();
        int level = calculatePlayerLevel(playerProfile);
        String globalPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getChat().getPlayerPrefix(gamePlayer.getPlayer()));

        return "§6[" + plugin.getLevel(level) + "✫]" + globalPrefix + "§7" + gamePlayer.getNickName() + CHAT_SEPARATOR + message;
    }

    /**
     * 计算玩家等级
     *
     * @param playerProfile 玩家数据
     * @return 玩家等级
     */
    private static int calculatePlayerLevel(PlayerProfile playerProfile) {
        return (playerProfile.getKills() * 2) +
                (playerProfile.getDestroyedBeds() * 10) +
                (playerProfile.getWins() * 15);
    }

    /**
     * 处理游戏中的聊天消息
     *
     * @param gamePlayer 游戏玩家对象
     * @param message    消息内容
     */
    public static void handleInGameChat(GamePlayer gamePlayer, String message) {
        if (gamePlayer.isSpectator()) {
            handleSpectatorChat(gamePlayer, message);
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        boolean isGlobalChat = message.startsWith(GLOBAL_CHAT_PREFIX);

        // 构建团队聊天消息
        String formattedMessage = buildTeamChatMessage(gamePlayer, gameTeam, message, isGlobalChat);

        // 发送消息
        if (isGlobalChat) {
            if (gamePlayer.isShoutCooldown()) {
                gamePlayer.sendMessage(MessageUtil.color("&c喊话冷却中！"));
                return;
            }
            gameManager.broadcastMessage(formattedMessage);
            checkShoutCooldown(gamePlayer);
        } else {
            gameManager.broadcastTeamMessage(gameTeam, formattedMessage);
        }
    }

    /**
     * 处理观察者聊天
     *
     * @param gamePlayer 游戏玩家对象
     * @param message    消息内容
     */
    public static void handleSpectatorChat(GamePlayer gamePlayer, String message) {
        String spectatorMessage = SPECTATOR_PREFIX + "§f" + gamePlayer.getNickName() + CHAT_SEPARATOR + message;

        if (gamePlayer.getPlayer().hasPermission("azurabedwars.admin")) {
            gameManager.broadcastMessage(spectatorMessage);
        } else {
            gameManager.broadcastSpectatorMessage(spectatorMessage);
        }
    }

    /**
     * 构建团队聊天消息
     *
     * @param gamePlayer   游戏玩家对象
     * @param gameTeam     玩家所在团队
     * @param message      原始消息
     * @param isGlobalChat 是否为全局聊天
     * @return 格式化后的团队聊天消息
     */
    public static String buildTeamChatMessage(GamePlayer gamePlayer, GameTeam gameTeam, String message, boolean isGlobalChat) {

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
