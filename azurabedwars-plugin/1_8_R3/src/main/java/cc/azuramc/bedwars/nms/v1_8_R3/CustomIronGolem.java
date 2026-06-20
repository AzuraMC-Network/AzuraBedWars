package cc.azuramc.bedwars.nms.v1_8_R3;

import cc.azuramc.bedwars.api.game.IGameTeam;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 * @author an5w1r@163.com
 */
public class CustomIronGolem extends AbstractCustomEntity {

    protected CustomIronGolem(EntityInsentient entityInsentient, IGameTeam gameTeam) {
        super(entityInsentient, gameTeam);
    }

    public static LivingEntity spawn(Location loc, IGameTeam gameTeam, double speed, double health) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.IRON_GOLEM);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        getCustomEntityMap().put(entityInsentient, new CustomIronGolem(entityInsentient, gameTeam));

        entityInsentient.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entityInsentient.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entityInsentient.setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    @Override
    protected void setupAttributes() {
        entityInsentient.setSize(1.4F, 2.9F);
        ((Navigation) entityInsentient.getNavigation()).a(true);
    }

    @Override
    protected void setupGoals() {
        entityInsentient.goalSelector.a(1, new PathfinderGoalFloat(entityInsentient));
        entityInsentient.goalSelector.a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.5D, false));
        entityInsentient.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        entityInsentient.goalSelector.a(4, new PathfinderGoalRandomStroll(entityCreature, 1D));
        entityInsentient.goalSelector.a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    @Override
    protected void setupTargets() {
        // 被攻击时立即反击
        entityInsentient.targetSelector.a(1, new PathfinderGoalHurtByTarget(entityCreature, true));

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
