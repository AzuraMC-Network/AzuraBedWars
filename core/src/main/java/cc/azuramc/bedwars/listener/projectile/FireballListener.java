package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

/**
 * @author An5w1r@163.com
 */
public class FireballListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    private final String fireballCooldownMetadata = "GAME_FIREBALL_TIMER";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Material material = event.getMaterial();

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (material != XMaterial.FIRE_CHARGE.get()) {
                return;
            }

            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }

            // 检查冷却时间
            long lastFireballTime = getLastFireballTime(gamePlayer);
            long currentTime = System.currentTimeMillis();
            long cooldownTime = 1000;

            if (currentTime - lastFireballTime < cooldownTime) {
                gamePlayer.sendMessage("&c火焰弹发射冷却中");
                return;
            }

            // 减少物品
            reduceItemInHand(gamePlayer);

            // 缓慢
            PotionEffect slowness = new PotionEffect(XPotion.parseEffect("SLOWNESS").getXPotion().getPotionEffectType(), 20, 1, false, false);
            player.addPotionEffect(slowness);

            // 设置冷却时间
            gamePlayer.getPlayer().setMetadata(fireballCooldownMetadata, new FixedMetadataValue(AzuraBedWars.getInstance(), currentTime));

            // 发射火球
            launchFireball(gamePlayer);
        }
    }


    /**
     * 获取玩家上次使用火球的时间
     * @return 返回上次使用时间的时间戳，如果没有使用过返回0
     */
    private long getLastFireballTime(GamePlayer gamePlayer) {
        Player player  = gamePlayer.getPlayer();
        if (player.hasMetadata(fireballCooldownMetadata)) {
            return player.getMetadata(fireballCooldownMetadata).get(0).asLong();
        }
        return 0L;
    }

    /**
     * 发射火球
     */
    private void launchFireball(GamePlayer gamePlayer) {
        Fireball fireball = gamePlayer.getPlayer().launchProjectile(Fireball.class);
        Vector direction = gamePlayer.getPlayer().getEyeLocation().getDirection();
        fireball = AzuraBedWars.getInstance().getNmsAccess().setFireballDirection(fireball, direction);
        fireball.setVelocity(gamePlayer.getPlayer().getLocation().getDirection().normalize().multiply(0.8));
        fireball.setYield(2.5F);
        fireball.setBounce(false);
        fireball.setIsIncendiary(false);
        fireball.setMetadata(FireballHandler.FIREBALL_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), gamePlayer.getUuid()));
    }

    /**
     * 减少玩家手中的物品数量
     */
    private void reduceItemInHand(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (PlayerUtil.getItemInHand(player).getAmount() == 1) {
            PlayerUtil.setItemInHand(player, null);
        } else {
            PlayerUtil.getItemInHand(player).setAmount(PlayerUtil.getItemInHand(player).getAmount() - 1);
        }
        gamePlayer.updateInventory();
    }
}
