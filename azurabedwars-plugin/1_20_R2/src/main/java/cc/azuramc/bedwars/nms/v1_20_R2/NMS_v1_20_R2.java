package cc.azuramc.bedwars.nms.v1_20_R2;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.nms.NMSAccess;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.world.entity.projectile.EntityFireball;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_20_R2 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(Fireball fireball, Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.b = vector.getX() * 0.1D;
        fb.c = vector.getY() * 0.1D;
        fb.d = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health, double damage) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_20_R1$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health, damage);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health, double damage) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_20_R1$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health + ", damage: " + damage);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health, damage);
    }
}
