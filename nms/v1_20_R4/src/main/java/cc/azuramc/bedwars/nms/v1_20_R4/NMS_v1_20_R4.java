package cc.azuramc.bedwars.nms.v1_20_R4;

import cc.azuramc.bedwars.api.nms.MobGoals;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import net.minecraft.world.entity.projectile.EntityFireball;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftFireball;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_20_R4 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.d = vector.getX() * 0.1D;
        fb.e = vector.getY() * 0.1D;
        fb.f = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public MobGoals getGoals(Creature creature) {
        return new MobGoalsImpl(((CraftCreature) creature).getHandle());
    }
}
