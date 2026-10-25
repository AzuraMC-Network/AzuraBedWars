package cc.azuramc.bedwars.nms.v26_1;

import cc.azuramc.bedwars.api.nms.MobGoals;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v26_1 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(@NonNull Fireball fireball, @NonNull Vector vector) {
        fireball.setVelocity(new Vector(vector.getX() * 0.1D, vector.getY() * 0.1D, vector.getZ() * 0.1D));
        return fireball;
    }

    @Override
    public MobGoals getGoals(Creature creature) {
        return new MobGoalsImpl(((CraftCreature) creature).getHandle());
    }
}
