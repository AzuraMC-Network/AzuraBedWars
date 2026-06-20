package cc.azuramc.bedwars.nms.mojangnamespace;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

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
    protected IGameTeam gameTeam;

    protected AbstractCustomEntity(Mob mob, IGameTeam gameTeam) {
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
        getGoalSelector(mob).getAvailableGoals().clear();
        getTargetSelector(mob).getAvailableGoals().clear();
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

        IGamePlayer gamePlayer = AzuraBedWarsAPI.getPlayer(human.getBukkitEntity().getUniqueId());
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

    protected GoalSelector getTargetSelector(@NotNull Mob mob) {
        return mob.targetSelector;
    }

    protected GoalSelector getGoalSelector(@NotNull Mob mob) {
        return mob.goalSelector;
    }
}
