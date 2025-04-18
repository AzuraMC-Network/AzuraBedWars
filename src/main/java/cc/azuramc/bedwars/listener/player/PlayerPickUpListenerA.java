package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PlayerPickUpListenerA implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem().getItemStack();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (gameManager.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }


        if (itemStack.getType() == MaterialWrapper.BED()) {
            if (itemStack.hasItemMeta()) {
                Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
                return;
            }

            event.setCancelled(true);
            event.getItem().remove();
        }

        if (itemStack.getType() == MaterialWrapper.WOODEN_SWORD() || itemStack.getType() == MaterialWrapper.STONE_SWORD() || itemStack.getType() == MaterialWrapper.IRON_SWORD() || itemStack.getType() == MaterialWrapper.DIAMOND_SWORD()) {
            if (gamePlayer.getGameTeam().isHasSharpenedEnchant()) {
                itemStack.addEnchantment(EnchantmentWrapper.DAMAGE_ALL(), 1);
            }

            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (player.getInventory().getItem(i) != null) {
                    if (Objects.requireNonNull(player.getInventory().getItem(i)).getType() == MaterialWrapper.WOODEN_SWORD()) {
                        player.getInventory().setItem(i, new ItemStack(MaterialWrapper.AIR()));
                        break;
                    }
                }
            }
        }

        // 当玩家将要捡起铁锭/金锭/钻石/绿宝石
        if (itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT || itemStack.getType() == Material.DIAMOND || itemStack.getType() == Material.EMERALD) {
            // 玩家挂机状态不能拾取资源
            if (PlayerAFKListener.afk.get(player.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT) {

            int xp = itemStack.getAmount();

            if (itemStack.getType() == Material.GOLD_INGOT) {
                xp = xp * 3;
            }

            if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
                event.setCancelled(true);
                event.getItem().remove();

                SoundWrapper.playLevelUpSound(player);
                player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
            } else if (playerProfile.getGameModeType() == GameModeType.EXPERIENCE) {
                event.setCancelled(true);
                event.getItem().remove();

                SoundWrapper.playLevelUpSound(player);
                player.setLevel(player.getLevel() + xp);
            }

            if (itemStack.hasItemMeta()) {
                Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();
                for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof Player players) {
                        players.playSound(players.getLocation(), SoundWrapper.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);

                        if (GamePlayer.get(players.getUniqueId()).getPlayerProfile().getGameModeType() == GameModeType.DEFAULT) {
                            players.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
                        } else {
                            players.setLevel(players.getLevel() + xp);
                        }
                    }
                }
            }
        }

        if (itemStack.getType() == Material.DIAMOND) {
            if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
                return;
            }

            double xp = itemStack.getAmount() * 40;
            event.setCancelled(true);

            if (player.hasPermission("azurabedwars.xp.vip1")) {
                xp = xp + (xp * 1.1);
            } else if (player.hasPermission("azurabedwars.xp.vip2")) {
                xp = xp + (xp * 1.2);
            } else if (player.hasPermission("azurabedwars.xp.vip3")) {
                xp = xp + (xp * 1.4);
            } else if (player.hasPermission("azurabedwars.xp.vip4")) {
                xp = xp + (xp * 1.8);
            }

            event.getItem().remove();
            player.setLevel((int) (player.getLevel() + xp));
            SoundWrapper.playLevelUpSound(player);
        }

        if (itemStack.getType() == Material.EMERALD) {
            if (playerProfile.getGameModeType() == GameModeType.DEFAULT) {
                return;
            }

            double xp = itemStack.getAmount() * 80;
            event.setCancelled(true);

            if (player.hasPermission("azurabedwars.xp.vip1")) {
                xp = xp + (xp * 1.1);
            } else if (player.hasPermission("azurabedwars.xp.vip2")) {
                xp = xp + (xp * 1.2);
            } else if (player.hasPermission("azurabedwars.xp.vip3")) {
                xp = xp + (xp * 1.4);
            } else if (player.hasPermission("azurabedwars.xp.vip4")) {
                xp = xp + (xp * 1.8);
            }

            event.getItem().remove();
            player.setLevel((int) (player.getLevel() + xp));
            SoundWrapper.playLevelUpSound(player);
        }
    }
}
