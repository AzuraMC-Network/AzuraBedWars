package cc.azuramc.bedwars.nms.v1_14_R1;

import cc.azuramc.bedwars.api.nms.MobGoals;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author an5w1r@163.com
 */
public class MobGoalsImpl implements MobGoals {

    private static final Field[] GOAL_COLLECTIONS;

    static {
        try {
            GOAL_COLLECTIONS = new Field[]{PathfinderGoalSelector.class.getDeclaredField("d")};
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
        for (Field field : GOAL_COLLECTIONS) {
            field.setAccessible(true);
        }
    }

    private final EntityCreature mob;

    public MobGoalsImpl(EntityCreature mob) {
        this.mob = mob;
    }

    private static void clear(PathfinderGoalSelector selector) {
        try {
            for (Field field : GOAL_COLLECTIONS) {
                ((Collection<?>) field.get(selector)).clear();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public MobGoals clearGoals() {
        clear(mob.goalSelector);
        return this;
    }

    @Override
    public MobGoals clearTargets() {
        clear(mob.targetSelector);
        return this;
    }

    @Override
    public MobGoals floatInWater(int priority) {
        mob.goalSelector.a(priority, new PathfinderGoalFloat(mob));
        return this;
    }

    @Override
    public MobGoals meleeAttack(int priority, double speed) {
        mob.goalSelector.a(priority, new PathfinderGoalMeleeAttack(mob, speed, false));
        return this;
    }

    @Override
    public MobGoals moveTowardsTarget(int priority, double speed, float range) {
        mob.goalSelector.a(priority, new PathfinderGoalMoveTowardsTarget(mob, speed, range));
        return this;
    }

    @Override
    public MobGoals randomStroll(int priority, double speed) {
        mob.goalSelector.a(priority, new PathfinderGoalRandomStroll(mob, speed));
        return this;
    }

    @Override
    public MobGoals randomLookAround(int priority) {
        mob.goalSelector.a(priority, new PathfinderGoalRandomLookaround(mob));
        return this;
    }

    @Override
    public MobGoals hurtByTarget(int priority) {
        mob.targetSelector.a(priority, new PathfinderGoalHurtByTarget(mob));
        return this;
    }

    @Override
    public MobGoals nearestTarget(int priority, Class<? extends LivingEntity> type, Predicate<LivingEntity> filter) {
        mob.targetSelector.a(priority, new PathfinderGoalNearestAttackableTarget<>(mob, EntityLiving.class, 20, true, false,
                entity -> type.isInstance(entity.getBukkitEntity()) && filter.test((LivingEntity) entity.getBukkitEntity())));
        return this;
    }
}
