package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameLoadEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.ReconnectState;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        // 正在运行的游戏：有断线快照则允许重连。
        // 注意 登录阶段不创建 GamePlayer 否则被拒登录的玩家不会触发 Quit 事件
        if (gameManager.getGameState() == GameState.RUNNING
                && gameManager.hasReconnectState(player.getUniqueId())) {
            event.allow();
            return;
        }

        // 检查是否有管理员权限
        boolean hasAdminPermission = player.hasPermission("azurabedwars.admin");

        if (hasAdminPermission) {
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            // 登录成功后才创建 GamePlayer，保证 GAME_PLAYERS 只含真正进服的在线玩家
            gamePlayer = GamePlayer.create(player);
        }

        // 重连：恢复断线快照（队伍 + 本局状态） 使后续重连判定/恢复生效
        ReconnectState reconnectState = gameManager.takeReconnectState(player.getUniqueId());
        if (reconnectState != null) {
            reconnectState.restoreTo(gamePlayer, gameManager);
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
                // 有权限的玩家强行加入但不添加到gameManager，直接设为观察者
                gameManager.addPlayer(gamePlayer);
                gamePlayer.setSpectator();
                return;
            }
        }
        gameManager.addPlayer(gamePlayer);
    }

    /**
     * 监听游戏加载事件，设置服务器最大玩家数限制
     */
    @EventHandler
    public void onGameLoading(BedwarsGameLoadEvent.Post event) {
        // 根据游戏最大玩家数设置服务器人数上限
        serverMaxPlayers = event.getGameManager().getMaxPlayers();
    }

    /**
     * 监听服务器列表ping事件，设置显示的最大玩家数
     */
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        // 设置显示的最大玩家数为bedwars管理的最大人数限制
        event.setMaxPlayers(serverMaxPlayers);
    }
}
