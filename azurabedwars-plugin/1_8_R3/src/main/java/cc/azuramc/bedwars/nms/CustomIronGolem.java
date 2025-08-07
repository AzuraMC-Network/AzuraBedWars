package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.CustomEntityManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

@Getter
public class CustomIronGolem extends EntityIronGolem {

    private GameTeam gameTeam;

    private CustomIronGolem(World world, GameTeam gameTeam) {
        super(world);
        this.gameTeam = gameTeam;
        clearGoals();
        setupAttributes();
        setupGoals();
        setupTargets();
    }

    private void clearGoals() {
        try {
            Field b = PathfinderGoalSelector.class.getDeclaredField("b");
            Field c = PathfinderGoalSelector.class.getDeclaredField("c");
            b.setAccessible(true);
            c.setAccessible(true);
            b.set(this.goalSelector, new UnsafeList<>());
            c.set(this.goalSelector, new UnsafeList<>());
            b.set(this.targetSelector, new UnsafeList<>());
            c.set(this.targetSelector, new UnsafeList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAttributes() {
        this.setSize(1.4F, 2.9F);
        ((Navigation) this.getNavigation()).a(true);
    }

    private void setupGoals() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.5D, false));
        this.goalSelector.a(3, new PathfinderGoalMoveTowardsTarget(this, 1.0D, 20.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 1D));
        this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));
    }

    private void setupTargets() {
        // 被攻击时立即反击
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));

        // 主动寻找玩家目标
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 20, true, false,
                human -> human != null && human.isAlive()
                        && !gameTeam.isInTeam(GamePlayer.get(human.getUniqueID()))
                        && !GamePlayer.get(human.getUniqueID()).isSpectator()));

        // 主动寻找铁傀儡目标
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, CustomIronGolem.class, 20, true, false,
                golem -> golem != null && golem.getGameTeam() != gameTeam));

        // 主动寻找蠹虫目标
        this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget<>(this, CustomSilverfish.class, 20, true, false,
                customSilverfish -> customSilverfish != null && customSilverfish.getGameTeam() != gameTeam));
    }

    public static LivingEntity spawn(Location loc, GameTeam gameTeam, double speed, double health, int despawn) {
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
        CustomIronGolem entity = new CustomIronGolem(world, gameTeam);

        entity.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        CraftLivingEntity craft = (CraftLivingEntity) entity.getBukkitEntity();
        craft.setRemoveWhenFarAway(false);

//        String name = "{TeamColor}{despawn}s &8[ {TeamColor}{health}&8]"
//                .replace("{TeamColor}", gameTeam.getChatColor().toString())
//                .replace("{despawn}", String.valueOf(despawn))
//                .replace("{health}", String.valueOf((int) health));
//        entity.setCustomName(name);
        entity.setCustomNameVisible(true);

        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return craft;
    }

    @Override
    protected void dropDeathLoot(boolean flag, int i) {
    }

    @Override
    public void die() {
        super.die();
        gameTeam = null;
        CustomEntityManager.getCustomEntityMap().remove(this.getUniqueID());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        gameTeam = null;
        CustomEntityManager.getCustomEntityMap().remove(this.getUniqueID());
    }
}
