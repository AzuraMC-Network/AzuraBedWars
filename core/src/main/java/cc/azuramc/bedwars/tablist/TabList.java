package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LuckPermsUtil;
import cc.azuramc.bedwars.util.VaultUtil;

/**
 * @author an5w1r@163.com
 */
public class TabList {
    
    /**
     * 编辑玩家的TabList显示名称
     * 如果玩家有队伍，显示格式为 "队伍颜色队伍名 | 玩家名"
     * 如果玩家没有队伍，只显示玩家名
     *
     * @param gamePlayer 目标玩家
     */
    public static void changeTabListNameInGame(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            // 如果GamePlayer为空，使用默认显示名
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
            return;
        }
        
        GameTeam gameTeam = gamePlayer.getGameTeam();
        String displayName;
        
        if (gameTeam != null) {
            displayName = gameTeam.getChatColor() + gameTeam.getName() + " | " + gamePlayer.getNickName();
        } else {
            // 如果玩家没有队伍，只显示玩家名
            displayName = "§7" + gamePlayer.getNickName();
        }
        
        gamePlayer.getPlayer().setPlayerListName(displayName);
    }

    public static void changeTabListNameWhenWaiting(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            // 如果GamePlayer为空，使用默认显示名
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
            return;
        }

        String prefix = "";

        if (LuckPermsUtil.isLoaded) {
            prefix = LuckPermsUtil.getPrefix(gamePlayer);
        } else if (!VaultUtil.chatIsNull) {
            prefix = VaultUtil.getPlayerPrefix(gamePlayer);
        }

        gamePlayer.getPlayer().setPlayerListName(prefix + gamePlayer.getNickName());
    }
    
    /**
     * 为所有在线玩家更新TabList显示名称
     */
    public static void updateAllTabListNames() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getPlayer() != null) {
                changeTabListNameInGame(gamePlayer);
            }
        }
    }
    
    /**
     * 为指定队伍的所有玩家更新TabList显示名称
     * 
     * @param gameTeam 目标队伍
     */
    public static void updateTeamTabListNames(GameTeam gameTeam) {
        if (gameTeam == null) {
            return;
        }
        
        for (GamePlayer gamePlayer : gameTeam.getGamePlayers()) {
            if (gamePlayer.getPlayer() != null) {
                changeTabListNameInGame(gamePlayer);
            }
        }
    }
    
    /**
     * 重置玩家的TabList显示名称为默认名称
     *
     * @param gamePlayer 目标玩家
     */
    public static void resetTabListName(GamePlayer gamePlayer) {
        if (gamePlayer != null) {
            gamePlayer.getPlayer().setPlayerListName(gamePlayer.getName());
        }
    }
}
