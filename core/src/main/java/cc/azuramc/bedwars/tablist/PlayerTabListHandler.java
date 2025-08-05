package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LuckPermsUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.VaultUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

/**
 * 玩家TabList处理类
 * 负责处理玩家TabList显示逻辑
 *
 * @author an5w1r@163.com
 */
public class PlayerTabListHandler {

    private final TabListManager tabListManager;

    public PlayerTabListHandler(TabListManager tabListManager) {
        this.tabListManager = tabListManager;
    }

    /**
     * 添加玩家到TabList
     */
    public void addPlayerToTab(GamePlayer gamePlayer, Map<GamePlayer, Team> teamMap) {
        Player player = gamePlayer.getPlayer();
        Scoreboard scoreboard = player.getScoreboard();

        String teamName = tabListManager.getTeamSorter().generateSortedTeamName(gamePlayer);
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.addEntry(gamePlayer.getName());
        teamMap.put(gamePlayer, team);

        updatePlayerTabList(gamePlayer, tabListManager.getGameManager());
        updatePlayerTag(gamePlayer, team);
    }

    /**
     * 从TabList移除玩家
     */
    public void removePlayerFromTab(GamePlayer gamePlayer, Map<GamePlayer, Team> teamMap) {
        Team team = teamMap.remove(gamePlayer);
        if (team != null) {
            team.removeEntry(gamePlayer.getName());
            team.unregister();
        }
    }

    /**
     * 更新玩家队伍
     */
    public void updatePlayerTeam(GamePlayer gamePlayer, Team oldTeam, String newTeamName, Map<GamePlayer, Team> teamMap) {
        // 注销之前的team
        oldTeam.removeEntry(gamePlayer.getName());
        oldTeam.unregister();

        // 注册新的team
        Team newTeam = gamePlayer.getPlayer().getScoreboard().registerNewTeam(newTeamName);
        teamMap.put(gamePlayer, newTeam);

        updatePlayerTabList(gamePlayer, tabListManager.getGameManager());
        updatePlayerTag(gamePlayer, newTeam);
    }

    /**
     * 更新玩家TabList显示名称
     */
    public void updatePlayerTabList(GamePlayer gamePlayer, GameManager gameManager) {
        Player player = gamePlayer.getPlayer();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        GameState gameState = gameManager.getGameState();

        if (gameState == GameState.WAITING) {
            updateWaitingStateDisplayName(gamePlayer, player);
        } else if (gameState == GameState.RUNNING || gameState == GameState.ENDING) {
            updateGameStateDisplayName(gamePlayer, player, gameTeam);
        }
    }

    /**
     * 更新等待状态的显示名称
     */
    private void updateWaitingStateDisplayName(GamePlayer gamePlayer, Player player) {
        String prefix = getPlayerPrefix(gamePlayer);
        player.setPlayerListName(prefix + gamePlayer.getNickName());
    }

    /**
     * 更新游戏状态的显示名称
     */
    private void updateGameStateDisplayName(GamePlayer gamePlayer, Player player, GameTeam gameTeam) {
        String displayName;

        if (gamePlayer.isRespawning()) {
            displayName = "&7" + gameTeam.getNameWithoutColor() + " | " + gamePlayer.getNickName();
        } else if (gamePlayer.isSpectator()) {
            displayName = "&7[旁观者] | " + gamePlayer.getNickName();
        } else {
            displayName = gameTeam.getName() + " | " + gamePlayer.getNickName();
        }

        player.setPlayerListName(MessageUtil.color(displayName));
    }

    /**
     * 更新玩家标签
     */
    public void updatePlayerTag(GamePlayer gamePlayer, Team team) {
        Player player = gamePlayer.getPlayer();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        Scoreboard scoreboard = player.getScoreboard();
        GameState gameState = tabListManager.getGameManager().getGameState();

        if (gameState == GameState.WAITING) {
            updateWaitingStateTag(gamePlayer, team, player, scoreboard);
        } else if (gameState == GameState.RUNNING || gameState == GameState.ENDING) {
            updateGameStateTag(gamePlayer, team, player, scoreboard, gameTeam);
        }
    }

    /**
     * 更新等待状态的标签
     */
    private void updateWaitingStateTag(GamePlayer gamePlayer, Team team, Player player, Scoreboard scoreboard) {
        String prefix = getPlayerPrefix(gamePlayer);
        team.setPrefix(MessageUtil.color(prefix));
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    /**
     * 更新游戏状态的标签
     */
    private void updateGameStateTag(GamePlayer gamePlayer, Team team, Player player, Scoreboard scoreboard, GameTeam gameTeam) {
        if (gamePlayer.isInvisible()) {
            team.removeEntry(gamePlayer.getName());
            return;
        }

        String prefix;
        if (gamePlayer.isRespawning()) {
            prefix = "&7" + gameTeam.getNameWithoutColor() + " | ";
        } else if (gamePlayer.isSpectator()) {
            prefix = "&7[旁观者] | ";
        } else {
            prefix = gameTeam.getName() + " | ";
        }

        team.setPrefix(MessageUtil.color(prefix));
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    /**
     * 获取玩家前缀
     */
    private String getPlayerPrefix(GamePlayer gamePlayer) {
        if (LuckPermsUtil.isLoaded) {
            return LuckPermsUtil.getPrefix(gamePlayer);
        } else if (!VaultUtil.chatIsNull) {
            return VaultUtil.getPlayerPrefix(gamePlayer);
        }
        return "";
    }
}
