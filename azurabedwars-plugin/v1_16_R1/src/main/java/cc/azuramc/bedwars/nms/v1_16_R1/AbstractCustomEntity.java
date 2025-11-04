package cc.azuramc.bedwars.nms.v1_16_R1;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public abstract class AbstractCustomEntity {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, AbstractCustomEntity> customEntityMap = new ConcurrentHashMap<>();

    private static Field pathfinderGoalWrappedSet;
    private static boolean reflectionInitialized = false;

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

    protected static void initializeReflection() {
        try {
            pathfinderGoalWrappedSet = PathfinderGoalSelector.class.getDeclaredField("d");
            pathfinderGoalWrappedSet.setAccessible(true);
            reflectionInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            reflectionInitialized = false;
        }
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
    protected boolean isValidPlayerTarget(EntityLiving entity) {
        if (!(entity instanceof EntityHuman human)) {
            return false;
        }

        if (human.getBukkitEntity().isDead()) {
            return false;
        }

        GamePlayer gamePlayer = GamePlayer.get(human.getBukkitEntity().getUniqueId());
        if (gamePlayer == null) {
            return false;
        }
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
    protected boolean isValidSilverfishTarget(EntityLiving entity) {
        if (!(entity instanceof EntitySilverfish fish)) {
            return false;
        }

        if (!AbstractCustomEntity.getCustomEntityMap().containsKey(fish)) {
            return false;
        }

        AbstractCustomEntity customFish = AbstractCustomEntity.getCustomEntityMap().get(fish);
        return customFish.getGameTeam() != gameTeam;
    }

    protected void clearGoals() {
        if (!reflectionInitialized) {
            return;
        }

        try {
            pathfinderGoalWrappedSet.set(entityInsentient.goalSelector, Sets.newLinkedHashSet());
            pathfinderGoalWrappedSet.set(entityInsentient.targetSelector, Sets.newLinkedHashSet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
