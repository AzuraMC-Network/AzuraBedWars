package cc.azuramc.bedwars.nms.v1_8_R3;

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

/**
 * @author an5w1r@163.com
 */
@Getter
public class CustomEntity {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, CustomEntity> customEntityMap = new ConcurrentHashMap<>();


    protected EntityInsentient entityInsentient;
    protected EntityCreature entityCreature;
    protected GameTeam gameTeam;

    protected CustomEntity(EntityInsentient entityInsentient, GameTeam gameTeam) {
        if (entityInsentient == null || gameTeam == null) {
            return;
        }

        this.entityInsentient = entityInsentient;
        this.entityCreature = (EntityCreature) entityInsentient;
        this.gameTeam = gameTeam;

        clearGoals();
        setupGoals();
        setupTargets();
    }

    public static LivingEntity spawnIronGolem(Location loc, GameTeam gameTeam, double speed, double health, double damage) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.IRON_GOLEM);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        customEntityMap.put(entityInsentient, new CustomEntity(entityInsentient, gameTeam));

        entityInsentient.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entityInsentient.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entityInsentient.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(damage);
        entityInsentient.setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    public static LivingEntity spawnSilverfish(Location loc, GameTeam gameTeam, double speed, double health, double damage) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.SILVERFISH);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        customEntityMap.put(entityInsentient, new CustomEntity(entityInsentient, gameTeam));

        entityInsentient.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entityInsentient.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entityInsentient.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(damage);
        entityInsentient.setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    protected void clearGoals() {
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

    protected void setupGoals() {
        entityInsentient.goalSelector.a(1, new PathfinderGoalFloat(entityInsentient));
        entityInsentient.goalSelector.a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.9D, false));
        entityInsentient.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        entityInsentient.goalSelector.a(4, new PathfinderGoalRandomStroll(entityCreature, 1.5D));
        entityInsentient.goalSelector.a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    protected void setupTargets() {
        // 被攻击时立即反击
        entityInsentient.targetSelector.a(1, new PathfinderGoalHurtByTarget(entityCreature, true));

        // 主动寻找玩家目标
        entityInsentient.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityHuman.class, 20, true, false,
                this::isValidPlayerTarget));

        // 主动寻找铁傀儡目标
        entityInsentient.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this.getEntityCreature(), EntityIronGolem.class, 20, true, false,
                this::isValidIronGolemTarget));

        // 主动寻找其他蠹虫目标
        entityInsentient.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget<>(this.getEntityCreature(), EntitySilverfish.class, 20, true, false,
                this::isValidSilverfishTarget));
    }

    /**
     * 判断玩家是否为有效攻击目标
     *
     * @param human 玩家实体
     * @return 是否为有效目标
     */
    protected boolean isValidPlayerTarget(EntityHuman human) {
        if (human == null || !human.isAlive()) {
            return false;
        }

        GamePlayer gamePlayer = GamePlayer.get(human.getUniqueID());
        if (gamePlayer == null) {
            return false;
        }

        // 不攻击同队玩家和观察者
        return !gameTeam.isInTeam(gamePlayer) && !gamePlayer.isSpectator();
    }

    /**
     * 判断铁傀儡是否为有效攻击目标
     *
     * @param golem 铁傀儡实体
     * @return 是否为有效目标
     */
    protected boolean isValidIronGolemTarget(EntityIronGolem golem) {
        if (golem == null) {
            return false;
        }

        CustomEntity customGolem = CustomEntity.getCustomEntityMap().get(golem);
        if (customGolem == null) {
            return false;
        }

        // 不攻击同队的铁傀儡
        return customGolem.getGameTeam() != gameTeam;
    }

    /**
     * 判断蠹虫是否为有效攻击目标
     *
     * @param fish 蠹虫实体
     * @return 是否为有效目标
     */
    protected boolean isValidSilverfishTarget(EntitySilverfish fish) {
        if (fish == null) {
            return false;
        }

        CustomEntity customFish = CustomEntity.getCustomEntityMap().get(fish);
        if (customFish == null) {
            return false;
        }

        // 不攻击同队的蠹虫
        return customFish.getGameTeam() != gameTeam;
    }
}
