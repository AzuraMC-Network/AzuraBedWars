package cc.azuramc.bedwars.nms.v1_18_R1;

import cc.azuramc.bedwars.api.game.IGameTeam;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class CustomSilverfish extends AbstractCustomEntity {

    protected CustomSilverfish(EntityInsentient entityInsentient, IGameTeam gameTeam) {
        super(entityInsentient, gameTeam);
    }

    public static LivingEntity spawn(Location loc, IGameTeam gameTeam, double speed, double health) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.SILVERFISH);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        getCustomEntityMap().put(entityInsentient, new CustomSilverfish(entityInsentient, gameTeam));

        Objects.requireNonNull(entityInsentient.a(GenericAttributes.d), "Spawn Silverfish speed parameter is null").a(speed);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.a), "Spawn Silverfish health parameter is null").a(health);
        entityInsentient.getBukkitEntity().setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    @Override
    protected void setupGoals() {
        getGoalSelector(entityInsentient).a(1, new PathfinderGoalFloat(entityInsentient));
        getGoalSelector(entityInsentient).a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.9D, false));
        getGoalSelector(entityInsentient).a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        getGoalSelector(entityInsentient).a(4, new PathfinderGoalRandomStroll(entityCreature, 1.5D));
        getGoalSelector(entityInsentient).a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    @Override
    protected void setupTargets() {
        // 被攻击时立即反击
        getTargetSelector(entityInsentient).a(1, new PathfinderGoalHurtByTarget(entityCreature));

        // 主动寻找玩家目标
        getTargetSelector(entityInsentient).a(2, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityHuman.class, 20, true, false,
                this::isValidPlayerTarget));

        // 主动寻找铁傀儡目标
        getTargetSelector(entityInsentient).a(3, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityIronGolem.class, 20, true, false,
                this::isValidIronGolemTarget));

        // 主动寻找其他蠹虫目标
        getTargetSelector(entityInsentient).a(4, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntitySilverfish.class, 20, true, false,
                this::isValidSilverfishTarget));
    }
}
