package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * @author An5w1r@163.com
 */
public interface NMSAccess {
    Fireball setFireballDirection(Fireball fireball, Vector vector);

    void registerCustomEntities();

    LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health, int despawn);

    LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health, int despawn, double damage);
}
