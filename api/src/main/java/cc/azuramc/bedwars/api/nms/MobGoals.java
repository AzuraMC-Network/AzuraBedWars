package cc.azuramc.bedwars.api.nms;

import org.bukkit.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Goal and target selectors of one mob, a lower priority runs first
 *
 * @author an5w1r@163.com
 */
public interface MobGoals {

    MobGoals clearGoals();

    MobGoals clearTargets();

    MobGoals floatInWater(int priority);

    MobGoals meleeAttack(int priority, double speed);

    MobGoals moveTowardsTarget(int priority, double speed, float range);

    MobGoals randomStroll(int priority, double speed);

    MobGoals randomLookAround(int priority);

    /** Targets the last attacker without alerting nearby mobs */
    MobGoals hurtByTarget(int priority);

    /** Targets the nearest visible entity of the type that passes the filter */
    MobGoals nearestTarget(int priority, Class<? extends LivingEntity> type, Predicate<LivingEntity> filter);
}
