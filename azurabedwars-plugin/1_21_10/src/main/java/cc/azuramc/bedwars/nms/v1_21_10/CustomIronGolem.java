package cc.azuramc.bedwars.nms.v1_21_10;

import cc.azuramc.bedwars.game.GameTeam;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class CustomIronGolem extends AbstractCustomEntity {

    protected CustomIronGolem(Mob entityInsentient, GameTeam gameTeam) {
        super(entityInsentient, gameTeam);
    }

    public static LivingEntity spawn(Location loc, GameTeam gameTeam, double speed, double health) {
        Entity bukkitEntity = EntityConverter.spawnBukkitEntity(loc, EntityType.IRON_GOLEM);
        CraftLivingEntity craftLivingEntity = (CraftLivingEntity) bukkitEntity;
        Mob mob = EntityConverter.bukkitToNms(bukkitEntity);

        if (mob == null) {
            return null;
        }

        getCustomEntityMap().put(mob, new CustomIronGolem(mob, gameTeam));

        LivingEntity bukkitLivingEntity = (LivingEntity) bukkitEntity;
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.MOVEMENT_SPEED), "Spawn IronGolem speed parameter is null").setBaseValue(speed);
        Objects.requireNonNull(bukkitLivingEntity.getAttribute(Attribute.MAX_HEALTH), "Spawn IronGolem health parameter is null").setBaseValue(health);
        mob.getBukkitEntity().setCustomNameVisible(true);
        craftLivingEntity.setRemoveWhenFarAway(false);
        return craftLivingEntity;
    }

    @Override
    protected void setupGoals() {
        mob.goalSelector.addGoal(1, new FloatGoal(mob));
        mob.goalSelector.addGoal(2, new MeleeAttackGoal(pathfinderMob, 1.5D, false));
        mob.goalSelector.addGoal(3, new MoveTowardsTargetGoal(pathfinderMob, 1.0D, 20.0F));
        mob.goalSelector.addGoal(4, new RandomStrollGoal(pathfinderMob, 1D));
        mob.goalSelector.addGoal(5, new RandomLookAroundGoal(pathfinderMob));
    }

    @Override
    protected void setupTargets() {
        // 被攻击时立即反击
        mob.targetSelector.addGoal(1, new HurtByTargetGoal(pathfinderMob));

        // 主动寻找玩家目标
        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(pathfinderMob, Player.class, 20, true, false,
                this::isValidPlayerTarget));

        // 主动寻找铁傀儡目标
        mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(pathfinderMob, IronGolem.class, 20, true, false,
                this::isValidIronGolemTarget));

        // 主动寻找其他蠹虫目标
        mob.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(pathfinderMob, Silverfish.class, 20, true, false,
                this::isValidSilverfishTarget));
    }

}
