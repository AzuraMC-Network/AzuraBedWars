package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 自定义生物伤害监听器
 * <p>
 * 专门处理自定义生物（铁傀儡和蠹虫）与玩家、投掷物之间的伤害逻辑
 * 包括队伍友军保护机制，防止同队伍之间的误伤
 * </p>
 *
 * @author an5w1r@163.com
 */
public class CustomEntityDamageListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理自定义生物相关的伤害事件
     *
     * @param event 实体间伤害事件
     */
    @EventHandler
    public void onCustomEntityDamage(EntityDamageByEntityEvent event) {
        // 检查是否游戏中
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Entity entity = event.getEntity();
        Entity attacker = event.getDamager();

        // 检查受击者是否为自定义生物
        if (entity instanceof IronGolem || entity instanceof Silverfish) {
            handleCustomEntityAsVictim(event, entity, attacker);
            return;
        }

        // 检查攻击者是否为自定义生物，受击者为玩家
        if ((attacker instanceof IronGolem || attacker instanceof Silverfish) && entity instanceof Player) {
            handleCustomEntityAttackPlayer(event, attacker, (Player) entity);
        }
    }

    /**
     * 处理自定义生物作为受击者的情况
     *
     * @param event    伤害事件
     * @param entity   被攻击的自定义生物
     * @param attacker 攻击者
     */
    private void handleCustomEntityAsVictim(EntityDamageByEntityEvent event, Entity entity, Entity attacker) {
        // 攻击者为玩家
        if (attacker instanceof Player) {
            handlePlayerAttackCustomEntity(event, entity, (Player) attacker);
        }
        // 攻击者为投掷物
        else if (attacker instanceof Projectile) {
            handleProjectileAttackCustomEntity(event, entity, (Projectile) attacker);
        }
        // 攻击者为自定义生物
        else if (attacker instanceof IronGolem || attacker instanceof Silverfish) {
            handleCustomEntityAttackCustomEntity(event, entity, attacker);
        }
    }

    /**
     * 处理玩家攻击自定义生物
     *
     * @param event    伤害事件
     * @param entity   被攻击的自定义生物
     * @param attacker 攻击者玩家
     */
    private void handlePlayerAttackCustomEntity(EntityDamageByEntityEvent event, Entity entity, Player attacker) {
        CustomEntityManager customEntityManager = CustomEntityManager.getCustomEntityMap().get(entity.getUniqueId());

        if (customEntityManager == null) {
            return;
        }

        GamePlayer attackerGamePlayer = GamePlayer.get(attacker.getUniqueId());

        // 如果攻击者不是GamePlayer或是旁观者 取消伤害
        if (attackerGamePlayer == null || attackerGamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        GameTeam entityTeam = customEntityManager.getGameTeam();
        GameTeam attackerTeam = attackerGamePlayer.getGameTeam();

        // 如果玩家攻击自己队伍的生物 取消伤害
        if (entityTeam != null && entityTeam.equals(attackerTeam)) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理投掷物攻击自定义生物
     *
     * @param event      伤害事件
     * @param entity     被攻击的自定义生物
     * @param projectile 投掷物
     */
    private void handleProjectileAttackCustomEntity(EntityDamageByEntityEvent event, Entity entity, Projectile projectile) {
        CustomEntityManager customEntityManager = CustomEntityManager.getCustomEntityMap().get(entity.getUniqueId());

        if (customEntityManager == null) {
            return;
        }

        // 检查投掷物发射者是否为玩家
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }

        GamePlayer shooterGamePlayer = GamePlayer.get(shooter.getUniqueId());

        // 如果发射者不是GamePlayer或是旁观者 取消伤害
        if (shooterGamePlayer == null || shooterGamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        GameTeam entityTeam = customEntityManager.getGameTeam();
        GameTeam shooterTeam = shooterGamePlayer.getGameTeam();

        // 如果投掷物发射者与被攻击生物属于同一队伍 取消伤害
        if (entityTeam != null && entityTeam.equals(shooterTeam)) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理自定义生物攻击自定义生物
     *
     * @param event    伤害事件
     * @param entity   被攻击的自定义生物
     * @param attacker 攻击者自定义生物
     */
    private void handleCustomEntityAttackCustomEntity(EntityDamageByEntityEvent event, Entity entity, Entity attacker) {
        CustomEntityManager victimEntityManager = CustomEntityManager.getCustomEntityMap().get(entity.getUniqueId());
        CustomEntityManager attackerEntityManager = CustomEntityManager.getCustomEntityMap().get(attacker.getUniqueId());

        if (victimEntityManager == null || attackerEntityManager == null) {
            return;
        }

        GameTeam victimTeam = victimEntityManager.getGameTeam();
        GameTeam attackerTeam = attackerEntityManager.getGameTeam();

        // 如果攻击者与被攻击者属于同一队伍 取消伤害
        if (victimTeam != null && victimTeam.equals(attackerTeam)) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理自定义生物攻击玩家
     *
     * @param event  伤害事件
     * @param entity 攻击者自定义生物
     * @param player 被攻击的玩家
     */
    private void handleCustomEntityAttackPlayer(EntityDamageByEntityEvent event, Entity entity, Player player) {
        CustomEntityManager customEntityManager = CustomEntityManager.getCustomEntityMap().get(entity.getUniqueId());
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (customEntityManager == null || gamePlayer == null) {
            return;
        }

        GamePlayer summoner = customEntityManager.getSummoner();
        GameTeam entityTeam = customEntityManager.getGameTeam();

        // 如果自定义生物的队伍和被攻击者不同
        if (gamePlayer.getGameTeam() != entityTeam) {
            gamePlayer.setLastDamage(summoner);
            return;
        }

        event.setCancelled(true);
    }
}
