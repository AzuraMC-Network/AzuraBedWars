package cc.azuramc.bedwars.nms.v26_2;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v26_2 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(@NonNull Fireball fireball, @NonNull Vector vector) {
        fireball.setVelocity(new Vector(vector.getX() * 0.1D, vector.getY() * 0.1D, vector.getZ() * 0.1D));
        return fireball;
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v26_2$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v26_2$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health);
    }
}
