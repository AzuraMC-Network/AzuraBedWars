package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.listener.projectile.FireballHandler;
import cc.azuramc.bedwars.util.LoadGameUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * @author an5w1r@163.com
 */
public class PlayerDamageListener implements Listener {

    private static final PlayerConfig.PlayerDeath CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getPlayerDeath();

    private static final long AUTO_RESPAWN_DELAY_TICKS = 10L;
    private static final double VOID_DAMAGE = 100.0D;
    private static final int ATTACK_DISPLAY_TITLE_TICKS = 10;

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final boolean arrowDisplayEnabled = gameManager.isArrowDisplayEnabled();
    private final boolean attackDisplayEnabled = gameManager.isAttackDisplayEnabled();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(LoadGameUtil.ITEM_SHOP_METADATA_KEY)
                || event.getEntity().hasMetadata(LoadGameUtil.UPGRADE_SHOP_METADATA_KEY)) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        if (gameManager.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            if (event.getCause() == DamageCause.VOID) {
                player.teleport(gameManager.getGameState() == GameState.WAITING
                        ? gameManager.getWaitingLocation() : gameManager.getRespawnLocation());
            }
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            return;
        }

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            if (event.getCause() == DamageCause.SUFFOCATION) {
                gamePlayer.getSpectatorTarget().tp();
            }
            return;
        }

        switch (event.getCause()) {
            case VOID:
                event.setDamage(VOID_DAMAGE);
                return;
            case FALL:
                if (player.hasMetadata(FireballHandler.NO_FALL_DAMAGE_METADATA)) {
                    player.removeMetadata(FireballHandler.NO_FALL_DAMAGE_METADATA, plugin.getPlugin());
                    event.setDamage(event.getFinalDamage() * CONFIG.getFireballFallenDamageRate());
                } else {
                    event.setDamage(event.getFinalDamage() * CONFIG.getNormalFallenDamageRate());
                }
                return;
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                event.setDamage(event.getFinalDamage() * CONFIG.getExplosionDamageRate());
                break;
            default:
                break;
        }

        if (gamePlayer.isInvisible()) {
            gamePlayer.endInvisibility();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setDroppedExp(0);

        Player player = event.getEntity();
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING && gamePlayer.getGameTeam() != null && !gamePlayer.isSpectator()) {
            EntityDamageEvent lastDamage = player.getLastDamageCause();
            gameManager.handlePlayerDeath(gamePlayer, lastDamage == null || lastDamage.isCancelled() ? DamageCause.VOID : lastDamage.getCause());
        }
        player.getInventory().clear();

        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), () -> {
            if (!player.isOnline()) {
                return;
            }
            player.spigot().respawn();
            PlayerUtil.hidePlayer(GamePlayer.getOnlinePlayers(), gamePlayer);
        }, AUTO_RESPAWN_DELAY_TICKS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (gameManager.getGameState() != GameState.RUNNING || !(event.getEntity() instanceof Player)) {
            return;
        }
        GamePlayer gamePlayer = GamePlayer.get((Player) event.getEntity());
        if (gamePlayer == null || gamePlayer.isSpectator()) {
            return;
        }

        Entity attacker = event.getDamager();
        if (attacker instanceof Player) {
            handlePlayerVsPlayerDamage(event, gamePlayer, (Player) attacker);
        } else if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof Player) {
            handleProjectileDamage(event, gamePlayer, (Projectile) attacker);
        }
    }

    private void handlePlayerVsPlayerDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Player attacker) {
        GamePlayer attackPlayer = GamePlayer.get(attacker);
        if (attackPlayer == null) {
            return;
        }
        if (attackPlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }
        if (gameTeam.isInTeam(attackPlayer)) {
            event.setCancelled(true);
            return;
        }

        attackPlayer.endRespawnProtection();
        if (gamePlayer.isRespawnProtected()) {
            return;
        }
        gamePlayer.setLastDamage(attackPlayer);

        if (attackDisplayEnabled && attackPlayer.isViewingAttackDamage()) {
            int finalDamage = Math.round((float) event.getFinalDamage());
            int remainingHealth = Math.max(Math.round((float) gamePlayer.getPlayer().getHealth()) - finalDamage, 0);
            attackPlayer.sendTitle(
                    "&r ",
                    "&b伤害 &f- &e" + finalDamage + "  &b血量 &f- &e" + remainingHealth +
                            "&f/&e" + Math.round((float) PlayerUtil.getMaxHealth(gamePlayer.getPlayer())),
                    1, ATTACK_DISPLAY_TITLE_TICKS, 5);
        }
    }

    private void handleProjectileDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Projectile projectile) {
        GamePlayer attackerPlayer = GamePlayer.get((Player) projectile.getShooter());
        if (attackerPlayer == null) {
            return;
        }

        // fireball damage is handled by FireballHandler
        if (projectile.getType() == EntityType.FIREBALL) {
            event.setCancelled(true);
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }
        if (gameTeam.isInTeam(attackerPlayer)) {
            event.setCancelled(true);
            return;
        }

        if (projectile.getType() != EntityType.ARROW) {
            return;
        }
        gamePlayer.setLastDamage(attackerPlayer);

        if (arrowDisplayEnabled && attackerPlayer.isViewingArrowDamage()) {
            attackerPlayer.sendMessage("&e&l目标玩家 &c&l" + gamePlayer.getPlayer().getName() + " &e&l剩余血量: &c&l"
                    + String.format("%.1f", gamePlayer.getPlayer().getHealth()) + " &c&l❤");
        }
    }
}
