package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;

/**
 * @author An5w1r@163.com
 */
public class DamageUtil {
    /**
     * 寻找真正的击杀者（包括辅助击杀）
     *
     * @param gamePlayer 游戏玩家
     * @return 击杀者
     */
    public static GamePlayer findKiller(GamePlayer gamePlayer) {
        Player killer = gamePlayer.getPlayer().getKiller();

        // 如果没有直接击杀者，尝试从辅助中获取
        if (killer == null) {
            GamePlayer gameKiller = gamePlayer.getLastDamager();
            if (gameKiller != null) {
                return gameKiller;
            }
        }

        return GamePlayer.get(killer);
    }

    public static Player findKiller(Player player) {
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            return null;
        }

        Player killer = gamePlayer.getPlayer().getKiller();

        // 如果没有直接击杀者，尝试从辅助中获取
        if (killer == null) {
            GamePlayer gameKiller = gamePlayer.getLastDamager();
            if (gameKiller != null) {
                return gameKiller.getPlayer();
            }
        }

        return killer;
    }
}
