package cc.azuramc.bedwars.nms.v1_15_R1;

import cc.azuramc.bedwars.game.GameTeam;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class CustomSilverfish extends AbstractCustomEntity {

    protected CustomSilverfish(EntityInsentient entityInsentient, GameTeam gameTeam) {
        super(entityInsentient, gameTeam);
    }

    public static LivingEntity spawn(Location loc, GameTeam gameTeam, double speed, double health) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.SILVERFISH);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        getCustomEntityMap().put(entityInsentient, new CustomSilverfish(entityInsentient, gameTeam));

        LivingEntity bukkitLivingEntity = (LivingEntity) bukkitEntity;
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED), "Spawn Silverfish speed parameter is null").setBaseValue(speed);
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH), "Spawn Silverfish health parameter is null").setBaseValue(health);
        entityInsentient.setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    @Override
    protected void setupGoals() {
        entityInsentient.goalSelector.a(1, new PathfinderGoalFloat(entityInsentient));
        entityInsentient.goalSelector.a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.9D, false));
        entityInsentient.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        entityInsentient.goalSelector.a(4, new PathfinderGoalRandomStroll(entityCreature, 1.5D));
        entityInsentient.goalSelector.a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    @Override
    protected void setupTargets() {
        // 被攻击时立即反击
        entityInsentient.targetSelector.a(1, new PathfinderGoalHurtByTarget(entityCreature));

        // 主动寻找玩家目标
        entityInsentient.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityHuman.class, 20, true, false,
                this::isValidPlayerTarget));

        // 主动寻找铁傀儡目标
        entityInsentient.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityIronGolem.class, 20, true, false,
                this::isValidIronGolemTarget));

        // 主动寻找其他蠹虫目标
        entityInsentient.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntitySilverfish.class, 20, true, false,
                this::isValidSilverfishTarget));
    }
}
