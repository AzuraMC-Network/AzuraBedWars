package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.nms.NMSMapping;
import cc.azuramc.bedwars.util.nms.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * @author An5w1r@163.com
 */
public class CompatibilityModeNMS implements NMSAccess {

    @Override
    public Fireball setFireballDirection(Fireball fireball, @NotNull Vector vector) {

        // if the server version is 1.21 or higher, we can use the new method to set the velocity directly (mojang changed the way to set fireball direction)
        if (VersionUtil.isLessThan(1, 21)) {
            fireball.setVelocity(new Vector(vector.getX() * 0.1D, vector.getY() * 0.1D, vector.getZ() * 0.1D));
            return fireball;
        }

        // compatibility mode for less than 1.21
        // using reflection to set the direction of the fireball
        try {
            Object nmsFireball = ReflectionUtil.getNMSObject(fireball);

            Field dirXField = NMSMapping.getField("EntityFireball", "dirX");
            Field dirYField = NMSMapping.getField("EntityFireball", "dirY");
            Field dirZField = NMSMapping.getField("EntityFireball", "dirZ");

            dirXField.set(nmsFireball, vector.getX() * 0.1D);
            dirYField.set(nmsFireball, vector.getY() * 0.1D);
            dirZField.set(nmsFireball, vector.getZ() * 0.1D);

            return fireball;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fireball direction using reflection", e);
        }
    }

    @Override
    public void registerCustomEntities() {

    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health) {
        return null;
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health, double damage) {
        return null;
    }

}
