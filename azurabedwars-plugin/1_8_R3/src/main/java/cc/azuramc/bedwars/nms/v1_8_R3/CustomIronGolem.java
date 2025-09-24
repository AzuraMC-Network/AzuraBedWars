package cc.azuramc.bedwars.nms.v1_8_R3;

import cc.azuramc.bedwars.game.CustomEntityManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CustomIronGolem {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, CustomIronGolem> entityMap = new ConcurrentHashMap<>();

    private EntityInsentient entityInsentient;
    private EntityCreature entityCreature;
    private GameTeam gameTeam;

    private CustomIronGolem(EntityInsentient entityInsentient, GameTeam gameTeam) {
        if (entityInsentient == null) return;
        if (gameTeam == null) return;
        this.entityInsentient = entityInsentient;
        this.entityCreature = (EntityCreature) entityInsentient;
        this.gameTeam = gameTeam;

        clearGoals();
        setupAttributes();
        setupGoals();
        setupTargets();
    }

    public static LivingEntity spawn(Location loc, GameTeam gameTeam, double speed, double health) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.IRON_GOLEM);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        entityMap.put(entityInsentient, new CustomIronGolem(entityInsentient, gameTeam));

        entityInsentient.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entityInsentient.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entityInsentient.setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    private void clearGoals() {
        try {
            Field b = PathfinderGoalSelector.class.getDeclaredField("b");
            Field c = PathfinderGoalSelector.class.getDeclaredField("c");
            b.setAccessible(true);
            c.setAccessible(true);
            b.set(entityInsentient.goalSelector, new UnsafeList<>());
            c.set(entityInsentient.goalSelector, new UnsafeList<>());
            b.set(entityInsentient.targetSelector, new UnsafeList<>());
            c.set(entityInsentient.targetSelector, new UnsafeList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAttributes() {
        entityInsentient.setSize(1.4F, 2.9F);
        ((Navigation) entityInsentient.getNavigation()).a(true);
    }

    private void setupGoals() {
        entityInsentient.goalSelector.a(1, new PathfinderGoalFloat(entityInsentient));
        entityInsentient.goalSelector.a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.5D, false));
        entityInsentient.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        entityInsentient.goalSelector.a(4, new PathfinderGoalRandomStroll(entityCreature, 1D));
        entityInsentient.goalSelector.a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    private void setupTargets() {
        // 被攻击时立即反击
        entityInsentient.targetSelector.a(1, new PathfinderGoalHurtByTarget(entityCreature, true));

        // 主动寻找玩家目标
        entityInsentient.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityHuman.class, 20, true, false,
                human -> human != null && human.isAlive()
                        && !gameTeam.isInTeam(GamePlayer.get(human.getUniqueID()))
                        && !GamePlayer.get(human.getUniqueID()).isSpectator()));

        // 主动寻找铁傀儡目标
        entityInsentient.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityIronGolem.class, 20, true, false,
                golem -> golem != null && CustomIronGolem.getEntityMap().get(golem).getGameTeam() != gameTeam));

        // 主动寻找蠹虫目标
        entityInsentient.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntitySilverfish.class, 20, true, false,
                fish -> fish != null && CustomSilverfish.getEntityMap().get(fish).getGameTeam() != gameTeam));
    }

    public void die() {
        entityInsentient.die();
        gameTeam = null;
        CustomEntityManager.getCustomEntityMap().remove(entityInsentient.getUniqueID());
    }

    public void die(DamageSource source) {
        entityInsentient.die(source);
        gameTeam = null;
        CustomEntityManager.getCustomEntityMap().remove(entityInsentient.getUniqueID());
    }
}
