package cc.azuramc.bedwars.listener.chat;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.hook.LuckPermsUtil;
import cc.azuramc.bedwars.util.hook.VaultUtil;
import org.bukkit.Bukkit;
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
 *
 * @author an5w1r@163.com
 */
public class ChatListener implements Listener {

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
        if (gameManager.getGameState() == GameState.RUNNING && !gameManager.getTimeline().isEnded() && gamePlayer != null) {
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
        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), () -> gamePlayer.setShoutCooldown(false), chat().getGlobalChatCooldown() * 20L);
    }

    /**
     * 构建聊天消息
     *
     * @param gamePlayer 游戏玩家对象
     * @param message    原始消息
     * @return 格式化后的聊天消息
     */
    public static String buildChatMessage(GamePlayer gamePlayer, String message) {
        int level = calculatePlayerLevel(gamePlayer.getPlayerData());
        String globalPrefix = "";

        if (LuckPermsUtil.isLoaded) {
            globalPrefix = LuckPermsUtil.getPrefix(gamePlayer);
        } else if (!VaultUtil.chatIsNull) {
            globalPrefix = VaultUtil.getPlayerPrefix(gamePlayer);
        }

        return globalPrefix + "§6[" + gamePlayer.getPlayerData().getLevel() + "✫] " + "§7" + gamePlayer.getNickName() + " " + chat().getChatSeparator() + " " + message;
    }

    /**
     * 计算玩家等级
     *
     * @param playerData 玩家数据
     * @return 玩家等级
     */
    private static int calculatePlayerLevel(PlayerData playerData) {
        return (playerData.getKills() * 2) +
                (playerData.getDestroyedBeds() * 10) +
                (playerData.getWins() * 15);
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
        if (gameTeam == null) {
            return;
        }
        boolean isGlobalChat = message.startsWith(chat().getGlobalChatPrefix());

        // 构建团队聊天消息
        String formattedMessage = buildTeamChatMessage(gamePlayer, gameTeam, message, isGlobalChat);

        // 发送消息
        if (isGlobalChat) {
            if (gamePlayer.isShoutCooldown()) {
                gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getCommand().getShoutCooldown());
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
        String spectatorMessage = chat().getSpectatorPrefix() + "§f" + gamePlayer.getNickName() + chat().getChatSeparator() + message;

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

        return (isGlobalChat ? chat().getGlobalChatTag() : chat().getTeamChatTag()) + " " +
                gameTeam.getChatColor() +
                "[" +
                gameTeam.getName() +
                "] " +
                gamePlayer.getNickName() +
                chat().getChatSeparator() +
                (isGlobalChat ? message.substring(1) : message);
    }

    private static GameplayConfig.Chat chat() {
        return AzuraBedWars.getInstance().getGameplayConfig().getChat();
    }
}
