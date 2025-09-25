package cc.azuramc.bedwars.nms.v1_20_R1;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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

        Objects.requireNonNull(entityInsentient.a(GenericAttributes.d), "Spawn IronGolem speed parameter is null").a(speed);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.a), "Spawn IronGolem health parameter is null").a(health);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.f), "Spawn IronGolem damage parameter is null").a(damage);
        entityInsentient.getBukkitEntity().setCustomNameVisible(true);
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

        Objects.requireNonNull(entityInsentient.a(GenericAttributes.d), "Spawn Silverfish speed parameter is null").a(speed);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.a), "Spawn Silverfish health parameter is null").a(health);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.f), "Spawn Silverfish damage parameter is null").a(damage);
        entityInsentient.getBukkitEntity().setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    protected void clearGoals() {
        getGoalSelector(entityInsentient).b().clear();
        getTargetSelector(entityInsentient).b().clear();
    }

    protected void setupGoals() {
        getGoalSelector(entityInsentient).a(1, new PathfinderGoalFloat(entityInsentient));
        getGoalSelector(entityInsentient).a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.9D, false));
        getGoalSelector(entityInsentient).a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        getGoalSelector(entityInsentient).a(4, new PathfinderGoalRandomStroll(entityCreature, 1.5D));
        getGoalSelector(entityInsentient).a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    protected void setupTargets() {
        getTargetSelector(entityInsentient).a(1, new PathfinderGoalNearestAttackableTarget<>(
                entityCreature, EntityHuman.class, true, this::isValidPlayerTarget));
        getTargetSelector(entityInsentient).a(2, new PathfinderGoalNearestAttackableTarget<>(
                entityCreature, EntityIronGolem.class, true, this::isValidIronGolemTarget));
        getTargetSelector(entityInsentient).a(3, new PathfinderGoalNearestAttackableTarget<>(
                entityCreature, EntitySilverfish.class, true, this::isValidSilverfishTarget));
    }

    /**
     * 判断玩家是否为有效攻击目标
     *
     * @param entity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidPlayerTarget(EntityLiving entity) {
        if (!(entity instanceof EntityHuman human)) {
            return false;
        }

        if (human.getBukkitEntity().isDead()) {
            return false;
        }

        GamePlayer gamePlayer = GamePlayer.get(human.getBukkitEntity().getUniqueId());
        return !gameTeam.isInTeam(gamePlayer) && !gamePlayer.isSpectator();
    }

    /**
     * 判断铁傀儡是否为有效攻击目标
     *
     * @param entity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidIronGolemTarget(EntityLiving entity) {
        if (!(entity instanceof EntityIronGolem golem)) {
            return false;
        }

        if (!CustomEntity.getCustomEntityMap().containsKey(golem)) {
            return false;
        }

        CustomEntity customGolem = CustomEntity.getCustomEntityMap().get(golem);
        return customGolem.getGameTeam() != gameTeam;
    }

    /**
     * 判断蠹虫是否为有效攻击目标
     *
     * @param entity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidSilverfishTarget(EntityLiving entity) {
        if (!(entity instanceof EntitySilverfish fish)) {
            return false;
        }

        if (!CustomEntity.getCustomEntityMap().containsKey(fish)) {
            return false;
        }

        CustomEntity customFish = CustomEntity.getCustomEntityMap().get(fish);
        return customFish.getGameTeam() != gameTeam;
    }

    protected PathfinderGoalSelector getTargetSelector(@NotNull EntityInsentient entityInsentient) {
        return entityInsentient.bP;
    }

    protected PathfinderGoalSelector getGoalSelector(@NotNull EntityInsentient entityInsentient) {
        return entityInsentient.bO;
    }
}
