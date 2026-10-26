package cc.azuramc.bedwars.nms.v26_1;

import cc.azuramc.bedwars.api.nms.MobGoals;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.function.Predicate;

/**
 * @author an5w1r@163.com
 */
public class MobGoalsImpl implements MobGoals {

    private final PathfinderMob mob;

    public MobGoalsImpl(PathfinderMob mob) {
        this.mob = mob;
    }

    @Override
    public MobGoals clearGoals() {
        mob.goalSelector.getAvailableGoals().clear();
        return this;
    }

    @Override
    public MobGoals clearTargets() {
        mob.targetSelector.getAvailableGoals().clear();
        return this;
    }

    @Override
    public MobGoals floatInWater(int priority) {
        mob.goalSelector.addGoal(priority, new FloatGoal(mob));
        return this;
    }

    @Override
    public MobGoals meleeAttack(int priority, double speed) {
        mob.goalSelector.addGoal(priority, new MeleeAttackGoal(mob, speed, false));
        return this;
    }

    @Override
    public MobGoals moveTowardsTarget(int priority, double speed, float range) {
        mob.goalSelector.addGoal(priority, new MoveTowardsTargetGoal(mob, speed, range));
        return this;
    }

    @Override
    public MobGoals randomStroll(int priority, double speed) {
        mob.goalSelector.addGoal(priority, new RandomStrollGoal(mob, speed));
        return this;
    }

    @Override
    public MobGoals randomLookAround(int priority) {
        mob.goalSelector.addGoal(priority, new RandomLookAroundGoal(mob));
        return this;
    }

    @Override
    public MobGoals hurtByTarget(int priority) {
        mob.targetSelector.addGoal(priority, new HurtByTargetGoal(mob));
        return this;
    }

    @Override
    public MobGoals nearestTarget(int priority, Class<? extends org.bukkit.entity.LivingEntity> type, Predicate<org.bukkit.entity.LivingEntity> filter) {
        mob.targetSelector.addGoal(priority, new NearestAttackableTargetGoal<>(mob, LivingEntity.class, 20, true, false,
                (entity, level) -> type.isInstance(entity.getBukkitEntity()) && filter.test((org.bukkit.entity.LivingEntity) entity.getBukkitEntity())));
        return this;
    }
}
