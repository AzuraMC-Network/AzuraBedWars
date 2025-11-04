package cc.azuramc.bedwars.nms.v1_21_10;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public abstract class AbstractCustomEntity {
    @Getter
    private static ConcurrentHashMap<Mob, AbstractCustomEntity> customEntityMap = new ConcurrentHashMap<>();

    protected Mob mob;
    protected PathfinderMob pathfinderMob;
    protected GameTeam gameTeam;

    protected AbstractCustomEntity(Mob mob, GameTeam gameTeam) {
        if (mob == null || gameTeam == null) {
            return;
        }

        this.mob = mob;
        this.pathfinderMob = (PathfinderMob) mob;
        this.gameTeam = gameTeam;

        clearGoals();
        setupAttributes();
        setupGoals();
        setupTargets();
    }

    protected void clearGoals() {
        mob.goalSelector.getAvailableGoals().clear();
        mob.targetSelector.getAvailableGoals().clear();
    }

    protected void setupAttributes() {
    }

    protected abstract void setupGoals();

    protected abstract void setupTargets();

    /**
     * 判断玩家是否为有效攻击目标
     *
     * @param livingEntity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidPlayerTarget(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (!(livingEntity instanceof Player human)) {
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
     * @param livingEntity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidIronGolemTarget(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (!(livingEntity instanceof IronGolem golem)) {
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
     * @param livingEntity 实体
     * @return 是否为有效目标
     */
    protected boolean isValidSilverfishTarget(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (!(livingEntity instanceof Silverfish fish)) {
            return false;
        }

        if (!AbstractCustomEntity.getCustomEntityMap().containsKey(fish)) {
            return false;
        }

        AbstractCustomEntity customFish = AbstractCustomEntity.getCustomEntityMap().get(fish);
        return customFish.getGameTeam() != gameTeam;
    }
}
