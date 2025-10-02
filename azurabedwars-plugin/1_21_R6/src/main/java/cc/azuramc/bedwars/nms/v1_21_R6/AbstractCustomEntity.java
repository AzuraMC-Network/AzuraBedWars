package cc.azuramc.bedwars.nms.v1_21_R6;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntitySilverfish;
import net.minecraft.world.entity.player.EntityHuman;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public abstract class AbstractCustomEntity {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, AbstractCustomEntity> customEntityMap = new ConcurrentHashMap<>();

    protected EntityInsentient entityInsentient;
    protected EntityCreature entityCreature;
    protected GameTeam gameTeam;

    protected AbstractCustomEntity(EntityInsentient entityInsentient, GameTeam gameTeam) {
        if (entityInsentient == null || gameTeam == null) {
            return;
        }

        this.entityInsentient = entityInsentient;
        this.entityCreature = (EntityCreature) entityInsentient;
        this.gameTeam = gameTeam;

        clearGoals();
        setupAttributes();
        setupGoals();
        setupTargets();
    }

    protected void clearGoals() {
        getGoalSelector(entityInsentient).b().clear();
        getTargetSelector(entityInsentient).b().clear();
    }

    protected void setupAttributes() {
    }

    protected abstract void setupGoals();

    protected abstract void setupTargets();

    /**
     * 判断玩家是否为有效攻击目标
     *
     * @param entity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidPlayerTarget(EntityLiving entity, WorldServer worldServer) {
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
    protected boolean isValidIronGolemTarget(EntityLiving entity, WorldServer worldServer) {
        if (!(entity instanceof EntityIronGolem golem)) {
            return false;
        }

        if (!AbstractCustomEntity.getCustomEntityMap().containsKey(golem)) {
            return false;
        }

        AbstractCustomEntity customGolem = AbstractCustomEntity.getCustomEntityMap().get(golem);
        return customGolem.getGameTeam() != gameTeam;
    }

    /**
     * 判断蠹虫是否为有效攻击目标
     *
     * @param entity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidSilverfishTarget(EntityLiving entity, WorldServer worldServer) {
        if (!(entity instanceof EntitySilverfish fish)) {
            return false;
        }

        if (!AbstractCustomEntity.getCustomEntityMap().containsKey(fish)) {
            return false;
        }

        AbstractCustomEntity customFish = AbstractCustomEntity.getCustomEntityMap().get(fish);
        return customFish.getGameTeam() != gameTeam;
    }

    protected PathfinderGoalSelector getTargetSelector(@NotNull EntityInsentient entityInsentient) {
        return entityInsentient.cr;
    }

    protected PathfinderGoalSelector getGoalSelector(@NotNull EntityInsentient entityInsentient) {
        return entityInsentient.cq;
    }
}
