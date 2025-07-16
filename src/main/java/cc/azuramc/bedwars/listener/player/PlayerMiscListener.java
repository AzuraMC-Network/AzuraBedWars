package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.util.InvisibleUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author an5w1r@163.com
 */
public class PlayerMiscListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        gamePlayer.getPlayer().setItemInHand(null);
        gamePlayer.getPlayer().getInventory().setItemInMainHand(null);
    }

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

        // 检查是否是隐身药水
        if (event.getItem().hasItemMeta() && event.getItem().getItemMeta() instanceof PotionMeta potionMeta) {
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                if (effect.getType() == PotionEffectType.INVISIBILITY) {
                    // 是隐身药水，处理盔甲隐藏
                    handleInvisibilityPotion(player, effect.getDuration());
                    break;
                }
            }
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

    /**
     * 处理隐身药水效果
     * @param player 玩家
     * @param duration 药水持续时间（tick）
     */
    private void handleInvisibilityPotion(Player player, int duration) {
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null || gamePlayer.isSpectator()) {
            return;
        }

        // 检查InvisibleUtil是否正确初始化
        if (!InvisibleUtil.isInitialized()) {
            player.sendMessage("§c隐身盔甲功能不可用，请联系管理员");
            return;
        }

        // 延迟3tick后隐藏盔甲（确保药水效果已经生效）
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    try {
                        InvisibleUtil.hideArmor(player);
                        // 给玩家发送确认消息（可选）
                        MessageUtil.sendActionBar(player, "§7盔甲已隐藏");
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("隐藏盔甲时发生错误: " + e.getMessage());
                        player.sendMessage("§c隐藏盔甲失败，请联系管理员");
                    }
                }
            }
        }.runTaskLater(AzuraBedWars.getInstance(), 3);

        // 定时检查隐身效果状态
        new BukkitRunnable() {
            @Override
            public void run() {
                // 检查玩家是否还在线
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // 检查玩家是否还存在游戏中
                GamePlayer currentGamePlayer = GamePlayer.get(player);
                if (currentGamePlayer == null) {
                    cancel();
                    return;
                }

                // 检查玩家是否已经成为观察者
                if (currentGamePlayer.isSpectator()) {
                    cancel();
                    return;
                }

                // 检查游戏状态
                if (gameManager.getGameState() != GameState.RUNNING) {
                    cancel();
                    return;
                }

                // 检查玩家是否还有隐身效果
                if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    // 隐身效果结束，恢复盔甲显示
                    try {
                        InvisibleUtil.showArmor(player);
                        // 给玩家发送确认消息（可选）
                        MessageUtil.sendActionBar(player, "§7盔甲已恢复显示");
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("恢复盔甲显示时发生错误: " + e.getMessage());
                        player.sendMessage("§c恢复盔甲显示失败，请联系管理员");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 20L, 20L); // 1秒后开始检查，每1秒检查一次
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
