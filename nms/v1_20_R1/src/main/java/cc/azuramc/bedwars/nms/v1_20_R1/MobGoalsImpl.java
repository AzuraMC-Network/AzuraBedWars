package cc.azuramc.bedwars.nms.v1_20_R1;

import cc.azuramc.bedwars.api.nms.MobGoals;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import org.bukkit.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * @author an5w1r@163.com
 */
public class MobGoalsImpl implements MobGoals {

    private final EntityCreature mob;

    public MobGoalsImpl(EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public MobGoals clearGoals() {
        mob.bO.b().clear();
        return this;
    }

    @Override
    public MobGoals clearTargets() {
        mob.bP.b().clear();
        return this;
    }

    @Override
    public MobGoals floatInWater(int priority) {
        mob.bO.a(priority, new PathfinderGoalFloat(mob));
        return this;
    }

    @Override
    public MobGoals meleeAttack(int priority, double speed) {
        mob.bO.a(priority, new PathfinderGoalMeleeAttack(mob, speed, false));
        return this;
    }

    @Override
    public MobGoals moveTowardsTarget(int priority, double speed, float range) {
        mob.bO.a(priority, new PathfinderGoalMoveTowardsTarget(mob, speed, range));
        return this;
    }

    @Override
    public MobGoals randomStroll(int priority, double speed) {
        mob.bO.a(priority, new PathfinderGoalRandomStroll(mob, speed));
        return this;
    }

    @Override
    public MobGoals randomLookAround(int priority) {
        mob.bO.a(priority, new PathfinderGoalRandomLookaround(mob));
        return this;
    }

    @Override
    public MobGoals hurtByTarget(int priority) {
        mob.bP.a(priority, new PathfinderGoalHurtByTarget(mob));
        return this;
    }

    @Override
    public MobGoals nearestTarget(int priority, Class<? extends LivingEntity> type, Predicate<LivingEntity> filter) {
        mob.bP.a(priority, new PathfinderGoalNearestAttackableTarget<>(mob, EntityLiving.class, 20, true, false,
                entity -> type.isInstance(entity.getBukkitEntity()) && filter.test((LivingEntity) entity.getBukkitEntity())));
        return this;
    }
}
