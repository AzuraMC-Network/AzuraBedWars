package cc.azuramc.bedwars.nms.v1_11_R1;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.nms.NMSAccess;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.server.v1_11_R1.EntityFireball;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_11_R1 implements NMSAccess {

    public NMS_v1_11_R1() {
        AbstractCustomEntity.initializeReflection();
    }

    @Override
    public Fireball setFireballDirection(Fireball fireball, Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_11_R1$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_11_R1$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setWoolBlockData(Block block, byte data) {
        block.setData(data);
    }
}
