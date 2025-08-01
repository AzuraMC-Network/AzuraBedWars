package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsGameLoadEvent;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerJoinListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private static int serverMaxPlayers = 16;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // 如果是正在运行的游戏且玩家有团队，允许重连
        if (gameManager.getGameState() == GameState.RUNNING && GamePlayer.get(player.getUniqueId()).getGameTeam() != null) {
            event.allow();
            return;
        }

        // 检查是否有管理员权限
        boolean hasAdminPermission = player.hasPermission("azurabedwars.admin");

        if (hasAdminPermission) {
            // 有权限的玩家可以强行加入但不会被添加到gameManager
            event.allow();
        }

        int currentPlayers = Bukkit.getOnlinePlayers().size();
        if (currentPlayers >= serverMaxPlayers) {
            // 有权限的玩家可以强行加入 无视服务器人数限制
            if (!hasAdminPermission) {
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, "服务器已满");
                return;
            }
        }

        // 检查游戏是否已满
        if (!hasAdminPermission && GamePlayer.getOnlinePlayers().size() >= gameManager.getMaxPlayers()) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "游戏人数已满");
            return;
        }

        // 如果游戏正在运行且玩家没有权限
        if (gameManager.getGameState() == GameState.RUNNING && !hasAdminPermission) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "游戏已开始");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (GamePlayer.get(event.getUniqueId()) != null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.create(event.getUniqueId(), event.getName());
        if (gameManager.getGameState() == GameState.RUNNING) {
            gamePlayer.setSpectator();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            player.kickPlayer("玩家异常状态");
            return;
        }

        FastBoard board = new FastBoard(player);
        board.updateTitle("§e§l起床战争");
        board.updateLines("Test");
        gamePlayer.setBoard(board);

        // 检查是否是有权限的强行加入玩家
        boolean hasAdminPermission = player.hasPermission("azurabedwars.admin");
        boolean playerHasTeam = gamePlayer.getGameTeam() != null;

        if (gameManager.getGameState() == GameState.RUNNING) {
            // 如果是有权限的玩家且游戏正在运行且玩家没有团队，则不添加到gameManager
            if (hasAdminPermission && !playerHasTeam) {
                // 有权限的玩家强行加入但不添加到gameManager，直接设为观察者并且使用bukkit hide
                gamePlayer.setSpectator();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    PlayerUtil.hidePlayer(player, onlinePlayer);
                }
                return;
            }
        }

        // 正常情况下添加玩家到gameManager
        gameManager.addPlayer(gamePlayer);
    }

    /**
     * 监听游戏加载事件，设置服务器最大玩家数限制
     */
    @EventHandler
    public void onGameLoading(BedwarsGameLoadEvent event) {
        // 根据游戏最大玩家数设置服务器人数上限
        serverMaxPlayers = event.getMaxPlayers();
    }

    /**
     * 监听服务器列表ping事件，设置显示的最大玩家数
     */
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // 设置显示的最大玩家数为bed wars管理的最大人数限制
        event.setMaxPlayers(serverMaxPlayers);
    }
}
