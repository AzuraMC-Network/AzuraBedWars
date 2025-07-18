package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * @author An5w1r@163.com
 */
public class PlayerTntDamageListener implements Listener {

    private final double tntJumpBarycenterAlterationInY = 0.5;
    private final double tntJumpStrengthReductionConstant = 5;
    private final double tntJumpYAxisReductionConstant = 2;
    private final double tntDamageSelf = 1;
    private final double tntDamageTeammates = 5;
    private final double tntDamageOthers = 10;

    @EventHandler
    public void onTntExplosion(EntityDamageByEntityEvent event) {
        if (AzuraBedWars.getInstance().getGameManager().getGameState() != GameState.RUNNING) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof TNTPrimed tnt)) return;
        if (!(tnt.getSource() instanceof Player damager)) return;

        if (damager.equals(player)) {
            applySelfTntDamage(event, player, tnt);
            return;
        }

        applyTeamBasedDamage(event, player, damager);
    }

    private void applySelfTntDamage(EntityDamageByEntityEvent event, Player player, TNTPrimed tnt) {
        event.setDamage(tntDamageSelf);

        Vector distance = player.getLocation()
                .subtract(0, tntJumpBarycenterAlterationInY, 0)
                .toVector()
                .subtract(tnt.getLocation().toVector());

        Vector direction = distance.clone().normalize();
        double force = (tnt.getYield() * tnt.getYield()) / (tntJumpStrengthReductionConstant + distance.length());
        Vector resultingForce = direction.multiply(force);

        resultingForce.setY(resultingForce.getY() / (distance.length() + tntJumpYAxisReductionConstant));
        player.setVelocity(resultingForce);
    }

    private void applyTeamBasedDamage(EntityDamageByEntityEvent event, Player damaged, Player damager) {
        GamePlayer damagedGamePlayer = GamePlayer.get(damaged);
        GamePlayer damagerGamePlayer = GamePlayer.get(damager);

        if (damagedGamePlayer == null || damagerGamePlayer == null) return;

        GameTeam damagedTeam = damagedGamePlayer.getGameTeam();
        GameTeam damagerTeam = damagerGamePlayer.getGameTeam();

        if (damagedTeam != null && damagedTeam.equals(damagerTeam)) {
            event.setDamage(tntDamageTeammates);
        } else {
            event.setDamage(tntDamageOthers);
        }
    }
}
