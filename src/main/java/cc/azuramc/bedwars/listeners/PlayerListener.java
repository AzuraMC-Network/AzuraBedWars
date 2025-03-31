package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.guis.ItemShopGUI;
import cc.azuramc.bedwars.guis.ModeSelectionGUI;
import cc.azuramc.bedwars.spectator.SpectatorCompassGUI;
import cc.azuramc.bedwars.guis.TeamShopGUI;
import cc.azuramc.bedwars.spectator.SpectatorSettingGUI;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.utils.SoundUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.utils.EnchantmentUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (GamePlayer.get(event.getEntity().getUniqueId()).isSpectator()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void craftItem(PrepareItemCraftEvent event) {
        for (HumanEntity h : event.getViewers()) {
            if (h instanceof Player) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material interactingMaterial = event.getMaterial();

        if (game.getGameState() == GameState.WAITING) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                switch (interactingMaterial) {
                    case PAPER:
                        new ModeSelectionGUI(player).open();
                        return;
                    case SLIME_BALL:
                        // 回大厅
                        return;
                    default:
                        return;
                }
            }
        }

        if (game.getGameState() == GameState.RUNNING) {
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (event.getAction() == Action.PHYSICAL) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (gamePlayer.isSpectator() && event.getClickedBlock() != null) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("BED")) {
                    if (player.isSneaking()) {
                        ItemStack item = MaterialUtil.getItemInHand(player);
                        if (item != null && item.getType().isBlock()) {
                            return;
                        }
                    }

                    event.setCancelled(true);
                    return;
                }
            }

            if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && (gamePlayer.getSpectatorTarget() != null) && interactingMaterial == Material.COMPASS) {
                gamePlayer.getSpectatorTarget().tp();
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                Material material = event.getMaterial();
                if (material == MaterialUtil.COMPASS()) {
                    event.setCancelled(true);
                    if (!gamePlayer.isSpectator()) {
                        return;
                    }
                    new SpectatorCompassGUI(player).open();
                    return;
                } else if (material == MaterialUtil.REDSTONE_COMPARATOR()) {
                    new SpectatorSettingGUI(player).open();
                    return;
                } else if (material == Material.PAPER) {
                    event.setCancelled(true);
                    Bukkit.dispatchCommand(player, "queue join qc x");
                    return;
                } else if (material == MaterialUtil.SLIME_BALL()) {
                    event.setCancelled(true);
                    // back to lobby
                    return;
                } else if (material == MaterialUtil.BED()) {
                    event.setCancelled(true);
                    if (gamePlayer.isSpectator()) {
                        return;
                    }

                    int priority = game.getEventManager().currentEvent().getPriority();
                    if (priority > 2 || priority == 2 && game.getEventManager().getLeftTime() <= 120) {
                        player.sendMessage("§c开局已超过10分钟.");
                        return;
                    }

                    if (gameTeam.isUnbed()) {
                        player.sendMessage("§c已使用过回春床了");
                        return;
                    }

                    if (!gameTeam.isBedDestroy()) {
                        player.sendMessage("§c床仍然存在 无法使用回春床");
                        return;
                    }

                    if (player.getLocation().distance(gameTeam.getSpawn()) > 18) {
                        player.sendMessage("§c请靠近出生点使用!");
                        return;
                    }

                    BlockFace face = gameTeam.getBedFace();

                    if (face == BlockFace.NORTH) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialUtil.AIR());
                        l.getBlock().setType(MaterialUtil.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.SOUTH).getState();
                        bedFoot.setType(MaterialUtil.BED());
                        bedHead.setType(MaterialUtil.BED());
                        bedFoot.setRawData((byte) 0);
                        bedHead.setRawData((byte) 8);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.EAST) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialUtil.AIR());
                        l.getBlock().setType(MaterialUtil.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.WEST).getState();
                        bedFoot.setType(MaterialUtil.BED());
                        bedHead.setType(MaterialUtil.BED());
                        bedFoot.setRawData((byte) 1);
                        bedHead.setRawData((byte) 9);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.SOUTH) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialUtil.AIR());
                        l.getBlock().setType(MaterialUtil.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.NORTH).getState();
                        bedFoot.setType(MaterialUtil.BED());
                        bedHead.setType(MaterialUtil.BED());
                        bedFoot.setRawData((byte) 2);
                        bedHead.setRawData((byte) 10);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.WEST) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialUtil.AIR());
                        l.getBlock().setType(MaterialUtil.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.EAST).getState();
                        bedFoot.setType(MaterialUtil.BED());
                        bedHead.setType(MaterialUtil.BED());
                        bedFoot.setRawData((byte) 3);
                        bedHead.setRawData((byte) 11);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    }

                    if (MaterialUtil.getItemInHand(player).getAmount() == 1) {
                        player.getInventory().setItemInHand(null);
                    } else {
                        MaterialUtil.getItemInHand(player).setAmount(MaterialUtil.getItemInHand(player).getAmount() - 1);
                    }

                    gameTeam.setBedDestroy(false);
                    gameTeam.setUnbed(true);

                    player.sendMessage("§a使用回春床成功!");
                    game.broadcastSound(SoundUtil.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                    game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                    game.broadcastMessage(" ");
                    game.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c使用了回春床！");
                    game.broadcastMessage(" ");
                    game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                    return;
                } else if (material == MaterialUtil.FIREBALL()) {
                    event.setCancelled(true);
                    if (gamePlayer.isSpectator()) {
                        return;
                    }

                    if (Math.abs(System.currentTimeMillis() - (player.hasMetadata("Game FIREBALL TIMER") ? player.getMetadata("Game FIREBALL TIMER").get(0).asLong() : 0L)) < 1000) {
                        return;
                    }

                    if (MaterialUtil.getItemInHand(player).getAmount() == 1) {
                        player.getInventory().setItemInHand(null);
                    } else {
                        MaterialUtil.getItemInHand(player).setAmount(MaterialUtil.getItemInHand(player).getAmount() - 1);
                    }

                    player.setMetadata("Game FIREBALL TIMER", new FixedMetadataValue(AzuraBedWars.getInstance(), System.currentTimeMillis()));

                    Fireball fireball = player.launchProjectile(Fireball.class);
                    fireball.setVelocity(fireball.getVelocity().multiply(2));
                    fireball.setYield(3.0F);
                    fireball.setBounce(false);
                    fireball.setIsIncendiary(false);
                    fireball.setMetadata("Game FIREBALL", new FixedMetadataValue(AzuraBedWars.getInstance(), player.getUniqueId()));
                    return;
                } else if (material == MaterialUtil.WATER_BUCKET()) {
                    for (MapData.RawLocation rawLocation : game.getMapData().getShops()) {
                        if (rawLocation.toLocation().distance(player.getLocation()) <= 5) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    for (GameTeam gameTeam1 : game.getGameTeams()) {
                        if (gameTeam1.getSpawn().distance(player.getLocation()) <= 8) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.RUNNING && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameDrop(PlayerDropItemEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            Player player = event.getPlayer();
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            ItemStack itemStack = event.getItemDrop().getItemStack();

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_HELMET") || itemStack.getType().toString().endsWith("_CHESTPLATE") || itemStack.getType().toString().endsWith("_LEGGINGS") || itemStack.getType().toString().endsWith("_BOOTS")) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_AXE") || itemStack.getType().toString().endsWith("PICKAXE") || itemStack.getType() == Material.SHEARS) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_SWORD")) {
                if (itemStack.getType() == MaterialUtil.WOODEN_SWORD()) {
                    event.getItemDrop().remove();
                }

                itemStack.removeEnchantment(EnchantmentUtil.DAMAGE_ALL());
                int size = 0;
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack itemStack1 = player.getInventory().getItem(i);
                    if (itemStack1 != null && itemStack1.getType().toString().endsWith("_SWORD")) {
                        size++;
                    }
                }

                if (size == 0) {
                    Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> gamePlayer.giveSword(false), 8);
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem().getItemStack();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (itemStack.getType() == MaterialUtil.BED()) {
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().getDisplayName() != null) {
                return;
            }

            event.setCancelled(true);
            event.getItem().remove();
        }

        if (itemStack.getType() == MaterialUtil.WOODEN_SWORD() || itemStack.getType() == MaterialUtil.STONE_SWORD() || itemStack.getType() == MaterialUtil.IRON_SWORD() || itemStack.getType() == MaterialUtil.DIAMOND_SWORD()) {
            if (gamePlayer.getGameTeam().isSharpenedSwords()) {
                itemStack.addEnchantment(EnchantmentUtil.DAMAGE_ALL(), 1);
            }

            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (player.getInventory().getItem(i) != null) {
                    if (player.getInventory().getItem(i).getType() == MaterialUtil.WOODEN_SWORD()) {
                        player.getInventory().setItem(i, new ItemStack(MaterialUtil.AIR()));
                        break;
                    }
                }
            }
        }

        if (itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT) {
            double xp = itemStack.getAmount();

            if (itemStack.getType() == Material.GOLD_INGOT) {
                xp = xp * 3;
            }

            if (playerData.getModeType() == ModeType.DEFAULT) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
            } else if (playerData.getModeType() == ModeType.EXPERIENCE) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.setLevel((int) (player.getLevel() + xp));
            }

            if (itemStack.hasItemMeta()) {
                itemStack.getItemMeta().getDisplayName();
                for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof Player) {
                        Player players = (Player) entity;
                        players.playSound(players.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);

                        if (GamePlayer.get(players.getUniqueId()).getPlayerData().getModeType() == ModeType.DEFAULT) {
                            players.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
                        } else {
                            players.setLevel((int) (players.getLevel() + xp));
                        }
                    }
                }
            }
        }

        if (itemStack.getType() == Material.DIAMOND) {
            if (playerData.getModeType() == ModeType.DEFAULT) {
                return;
            }

            double xp = itemStack.getAmount() * 40;
            event.setCancelled(true);

            if (player.hasPermission("bw.xp.vip1")) {
                xp = xp + (xp * 1.1);
            } else if (player.hasPermission("bw.xp.vip2")) {
                xp = xp + (xp * 1.2);
            } else if (player.hasPermission("bw.xp.vip3")) {
                xp = xp + (xp * 1.4);
            } else if (player.hasPermission("bw.xp.vip4")) {
                xp = xp + (xp * 1.8);
            }

            event.getItem().remove();
            player.setLevel((int) (player.getLevel() + xp));
            player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
        }

        if (itemStack.getType() == Material.EMERALD) {
            if (playerData.getModeType() == ModeType.DEFAULT) {
                return;
            }

            double xp = itemStack.getAmount() * 80;
            event.setCancelled(true);

            if (player.hasPermission("bw.xp.vip1")) {
                xp = xp + (xp * 1.1);
            } else if (player.hasPermission("bw.xp.vip2")) {
                xp = xp + (xp * 1.2);
            } else if (player.hasPermission("bw.xp.vip3")) {
                xp = xp + (xp * 1.4);
            } else if (player.hasPermission("bw.xp.vip4")) {
                xp = xp + (xp * 1.8);
            }

            event.getItem().remove();
            player.setLevel((int) (player.getLevel() + xp));
            player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("/report")) {
            return;
        }

        if (message.startsWith("/queue join qc x")) {
            return;
        }

        if (!player.hasPermission("bw.*")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().hasMetadata("Shop")) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }
            new ItemShopGUI(player, 0, game).open();
            return;
        }

        if (event.getRightClicked().hasMetadata("Shop2")) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }

            new TeamShopGUI(player, game).open();
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (event.getItem().getType() != Material.POTION) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (MaterialUtil.getItemInHand(player).getType() == MaterialUtil.GLASS_BOTTLE()) {
                    MaterialUtil.setItemInHand(player, new ItemStack(MaterialUtil.AIR()));
                }
            }
        }.runTaskLater(AzuraBedWars.getInstance(), 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && game.getGameState() == GameState.RUNNING) {
            if (gamePlayer.isSpectator() && event.getRightClicked() instanceof Player && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
                event.setCancelled(true);
                if (GamePlayer.get(event.getRightClicked().getUniqueId()).isSpectator()) {
                    return;
                }

                gamePlayer.sendTitle(0, 20, 0, "§a正在旁观§7" + event.getRightClicked().getName(), "§a点击左键打开菜单  §c按Shift键退出");
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().setSpectatorTarget(event.getRightClicked());
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator() && (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) && player.getGameMode() == GameMode.SPECTATOR) {
            gamePlayer.sendTitle(0, 20, 0, "§e退出旁观模式", "");
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

                game.broadcastTeamTitle(gameTeam, 0, 8, 0, "", gameTeam.getChatColor() + gamePlayer.getDisplayname() + " 说: §c注意,我们的床有危险！");
                game.broadcastTeamSound(gameTeam, SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
                i++;
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0, 10L);
    }
}
