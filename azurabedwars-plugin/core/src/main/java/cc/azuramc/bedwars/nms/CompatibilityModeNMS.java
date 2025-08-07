package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.projectile.EntityFireball;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author An5w1r@163.com
 */
public class CompatibilityModeNMS implements NMSAccess {

    @Override
    public Fireball setFireballDirection(Fireball fireball, @NotNull Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.b = vector.getX() * 0.1D;
        fb.c = vector.getY() * 0.1D;
        fb.d = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public void registerCustomEntities() {

    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health, int despawn) {
        return null;
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health, int despawn, double damage) {
        return null;
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().c.a(packet);
    }
}
