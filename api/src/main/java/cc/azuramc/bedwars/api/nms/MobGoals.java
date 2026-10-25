package cc.azuramc.bedwars.api.nms;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Goal and target selectors of one mob, a lower priority runs first
 *
 * @author an5w1r@163.com
 */
@ApiStatus.Internal
public interface MobGoals {

    @NotNull
    MobGoals clearGoals();

    @NotNull
    MobGoals clearTargets();

    @NotNull
    MobGoals floatInWater(int priority);

    @NotNull
    MobGoals meleeAttack(int priority, double speed);

    @NotNull
    MobGoals moveTowardsTarget(int priority, double speed, float range);

    @NotNull
    MobGoals randomStroll(int priority, double speed);

    @NotNull
    MobGoals randomLookAround(int priority);

    /** Targets the last attacker without alerting nearby mobs */
    @NotNull
    MobGoals hurtByTarget(int priority);

    /** Targets the nearest visible entity of the type that passes the filter */
    @NotNull
    MobGoals nearestTarget(int priority, @NotNull Class<? extends LivingEntity> type, @NotNull Predicate<LivingEntity> filter);
}
