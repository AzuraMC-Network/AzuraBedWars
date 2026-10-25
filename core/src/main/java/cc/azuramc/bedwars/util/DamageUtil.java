package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * @author an5w1r@163.com
 */
public class DamageUtil {

    /**
     * The direct killer, or the enemy who hurt the player last
     */
    @Nullable
    public static GamePlayer findKiller(GamePlayer gamePlayer) {
        Player killer = gamePlayer.getPlayer().getKiller();
        GamePlayer gameKiller = killer != null ? GamePlayer.get(killer) : gamePlayer.getLastDamager();
        if (gameKiller == null || gameKiller.equals(gamePlayer)) {
            return null;
        }
        // damage records keep the instance from before a reconnect
        return GamePlayer.get(gameKiller.getUuid());
    }
}
