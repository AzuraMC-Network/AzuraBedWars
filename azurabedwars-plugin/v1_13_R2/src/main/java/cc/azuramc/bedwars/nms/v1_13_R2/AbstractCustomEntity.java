package cc.azuramc.bedwars.nms.v1_13_R2;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.server.v1_13_R2.*;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public abstract class AbstractCustomEntity {
    @Getter
    private static ConcurrentHashMap<EntityInsentient, AbstractCustomEntity> customEntityMap = new ConcurrentHashMap<>();

    private static Field pathfinderGoalSelectorFieldB;
    private static Field pathfinderGoalSelectorFieldC;
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
            pathfinderGoalSelectorFieldB = PathfinderGoalSelector.class.getDeclaredField("b");
            pathfinderGoalSelectorFieldC = PathfinderGoalSelector.class.getDeclaredField("c");
            pathfinderGoalSelectorFieldB.setAccessible(true);
            pathfinderGoalSelectorFieldC.setAccessible(true);
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

        AbstractCustomEntity customGolem = AbstractCustomEntity.getCustomEntityMap().get(golem);
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

        AbstractCustomEntity customFish = AbstractCustomEntity.getCustomEntityMap().get(fish);
        if (customFish == null) {
            return false;
        }

        // 不攻击同队的蠹虫
        return customFish.getGameTeam() != gameTeam;
    }

    protected void clearGoals() {
        if (!reflectionInitialized) {
            return;
        }

        try {
            pathfinderGoalSelectorFieldB.set(entityInsentient.goalSelector, Sets.newLinkedHashSet());
            pathfinderGoalSelectorFieldC.set(entityInsentient.goalSelector, Sets.newLinkedHashSet());
            pathfinderGoalSelectorFieldB.set(entityInsentient.targetSelector, Sets.newLinkedHashSet());
            pathfinderGoalSelectorFieldC.set(entityInsentient.targetSelector, Sets.newLinkedHashSet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
