package cc.azuramc.bedwars.nms.v1_19_R1;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import net.minecraft.world.entity.projectile.EntityFireball;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_19_R1 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.b = vector.getX() * 0.1D;
        fb.c = vector.getY() * 0.1D;
        fb.d = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v1_19_R1$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v1_19_R1$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health);
    }
}
