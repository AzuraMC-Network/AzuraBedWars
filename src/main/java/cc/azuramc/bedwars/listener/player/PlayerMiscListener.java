package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author an5w1r@163.com
 */
public class PlayerMiscListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void craftItem(PrepareItemCraftEvent event) {
        for (HumanEntity entity : event.getViewers()) {
            if (entity instanceof Player) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (gameManager.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (event.getItem().getType() != XMaterial.POTION.get()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerUtil.getItemInHand(player).getType() == XMaterial.GLASS_BOTTLE.get()) {
                    PlayerUtil.setItemInHand(player, new ItemStack(Material.AIR));
                }
            }
        }.runTaskLater(AzuraBedWars.getInstance(), 0);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator() && (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRST_PERSON)) && player.getGameMode() == GameMode.SPECTATOR) {
            gamePlayer.sendTitle("§e退出旁观模式", "", 0, 20, 0);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            return;
        }

        if (player.hasMetadata("等待上一次求救")) {
            return;
        }

        if (player.getLocation().getPitch() > -80) {
            return;
        }

        player.setMetadata("等待上一次求救", new FixedMetadataValue(AzuraBedWars.getInstance(), ""));

        GameTeam gameTeam = gamePlayer.getGameTeam();

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i > 5) {
                    player.removeMetadata("等待上一次求救", AzuraBedWars.getInstance());
                    cancel();
                    return;
                }

                gameManager.broadcastTeamTitle(gameTeam, "", gameTeam.getChatColor() + gamePlayer.getNickName() + " 说: §c注意,我们的床有危险！", 0, 8, 0);
                gameManager.broadcastTeamSound(gameTeam, XSound.UI_BUTTON_CLICK.get(), 1f, 1f);
                i++;
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0, 10L);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        Material blockType = null;
        if (block != null) {
            blockType = block.getType();
        }

        // 只对木箱进行判断
        if (blockType != XMaterial.CHEST.get()) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        for (GameTeam team : gameManager.getGameTeams()) {
            if (team.getSpawnLocation().distance(block.getLocation()) <= 18) {
                if (!team.getAlivePlayers().isEmpty() && !team.isInTeam(gamePlayer)) {
                    event.setCancelled(true);
                    player.sendMessage("§c只有该队伍的玩家可以打开这个箱子");
                }
                break;
            }
        }
    }
}
