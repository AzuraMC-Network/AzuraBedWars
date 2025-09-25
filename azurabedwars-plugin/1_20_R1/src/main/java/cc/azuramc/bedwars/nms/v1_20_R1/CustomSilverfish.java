package cc.azuramc.bedwars.nms.v1_20_R1;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CustomSilverfish {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, CustomSilverfish> entityMap = new ConcurrentHashMap<>();

    private EntityInsentient entityInsentient;
    private EntityCreature entityCreature;
    private GameTeam gameTeam;

    private CustomSilverfish(EntityInsentient entityInsentient, GameTeam gameTeam) {
        if (entityInsentient == null) return;
        if (gameTeam == null) return;
        this.entityInsentient = entityInsentient;
        this.entityCreature = (EntityCreature) entityInsentient;
        this.gameTeam = gameTeam;

        clearGoals();
        setupGoals();
        setupTargets();
    }

    public static LivingEntity spawn(Location loc, GameTeam gameTeam, double speed, double health, double damage) {

        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.SILVERFISH);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        EntityInsentient entityInsentient = EntityConverter.bukkitToNms(bukkitEntity);

        if (entityInsentient == null) {
            return null;
        }

        entityMap.put(entityInsentient, new CustomSilverfish(entityInsentient, gameTeam));

        Objects.requireNonNull(entityInsentient.a(GenericAttributes.d), "Spawn Silverfish speed parameter is null").a(speed);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.a), "Spawn Silverfish health parameter is null").a(health);
        Objects.requireNonNull(entityInsentient.a(GenericAttributes.f), "Spawn Silverfish damage parameter is null").a(damage);
        entityInsentient.getBukkitEntity().setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    private void clearGoals() {
        try {
            EntityConverter.getGoalSelector(entityInsentient).b().clear();
            EntityConverter.getTargetSelector(entityInsentient).b().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGoals() {
        entityInsentient.bO.a(1, new PathfinderGoalFloat(entityInsentient));
        entityInsentient.bO.a(2, new PathfinderGoalMeleeAttack(entityCreature, 1.9D, false));
        entityInsentient.bO.a(3, new PathfinderGoalMoveTowardsTarget(entityCreature, 1.0D, 20.0F));
        entityInsentient.bO.a(4, new PathfinderGoalRandomStroll(entityCreature, 1.5D));
        entityInsentient.bO.a(5, new PathfinderGoalRandomLookaround(entityCreature));
    }

    private void setupTargets() {
        // 被攻击时立即反击
        entityInsentient.bP.a(1, new PathfinderGoalHurtByTarget(entityCreature));

        // 主动寻找玩家目标
        entityInsentient.bP.a(2, new PathfinderGoalNearestAttackableTarget<>(entityCreature, EntityHuman.class, 20, true, false,
                human -> human != null && !human.getBukkitEntity().isDead()
                        && !gameTeam.isInTeam(GamePlayer.get(human.getBukkitEntity().getUniqueId()))
                        && !GamePlayer.get(human.getBukkitEntity().getUniqueId()).isSpectator()));

        // 主动寻找铁傀儡目标
        entityInsentient.bP.a(3, new PathfinderGoalNearestAttackableTarget<>(this.getEntityCreature(), EntityIronGolem.class, 20, true, false,
                golem -> golem != null && CustomIronGolem.getEntityMap().containsKey(golem)
                        && CustomIronGolem.getEntityMap().get(golem).getGameTeam() != gameTeam));

        // 主动寻找其他蠹虫目标
        entityInsentient.bP.a(4, new PathfinderGoalNearestAttackableTarget<>(this.getEntityCreature(), EntitySilverfish.class, 20, true, false,
                fish -> fish != null && CustomSilverfish.getEntityMap().containsKey(fish)
                        && CustomSilverfish.getEntityMap().get(fish).getGameTeam() != gameTeam));
    }
}
