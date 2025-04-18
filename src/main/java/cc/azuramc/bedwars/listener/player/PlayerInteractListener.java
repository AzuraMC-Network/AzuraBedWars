package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.gui.ModeSelectionGUI;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.gui.SpectatorCompassGUI;
import cc.azuramc.bedwars.spectator.gui.SpectatorSettingGUI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerInteractListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material interactingMaterial = event.getMaterial();

        if (gameManager.getGameState() == GameState.WAITING) {
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

        if (gameManager.getGameState() == GameState.RUNNING) {
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
                        ItemStack item = PlayerUtil.getItemInHand(player);
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
                if (material == MaterialWrapper.COMPASS()) {
                    event.setCancelled(true);
                    if (!gamePlayer.isSpectator()) {
                        return;
                    }
                    new SpectatorCompassGUI(player).open();
                    return;
                } else if (material == MaterialWrapper.REDSTONE_COMPARATOR()) {
                    new SpectatorSettingGUI(player).open();
                    return;
                } else if (material == Material.PAPER) {
                    event.setCancelled(true);
                    Bukkit.dispatchCommand(player, "azurabedwars nextgame");
                    return;
                } else if (material == MaterialWrapper.SLIME_BALL()) {
                    event.setCancelled(true);
                    // back to lobby
                    return;
                } else if (material == MaterialWrapper.BED()) {
                    event.setCancelled(true);
                    if (gamePlayer.isSpectator()) {
                        return;
                    }

                    int priority = gameManager.getGameEventManager().currentEvent().getPriority();
                    if (priority > 2 || priority == 2 && gameManager.getGameEventManager().getLeftTime() <= 120) {
                        player.sendMessage("§c开局已超过10分钟.");
                        return;
                    }

                    if (gameTeam.isHasBed()) {
                        player.sendMessage("§c已使用过回春床了");
                        return;
                    }

                    if (!gameTeam.isDestroyed()) {
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
                        l.getBlock().setType(MaterialWrapper.AIR());
                        l.getBlock().setType(MaterialWrapper.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.SOUTH).getState();
                        bedFoot.setType(MaterialWrapper.BED());
                        bedHead.setType(MaterialWrapper.BED());
                        bedFoot.setRawData((byte) 0);
                        bedHead.setRawData((byte) 8);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.EAST) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialWrapper.AIR());
                        l.getBlock().setType(MaterialWrapper.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.WEST).getState();
                        bedFoot.setType(MaterialWrapper.BED());
                        bedHead.setType(MaterialWrapper.BED());
                        bedFoot.setRawData((byte) 1);
                        bedHead.setRawData((byte) 9);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.SOUTH) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialWrapper.AIR());
                        l.getBlock().setType(MaterialWrapper.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.NORTH).getState();
                        bedFoot.setType(MaterialWrapper.BED());
                        bedHead.setType(MaterialWrapper.BED());
                        bedFoot.setRawData((byte) 2);
                        bedHead.setRawData((byte) 10);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    } else if (face == BlockFace.WEST) {
                        Location l = gameTeam.getBedHead().getLocation();
                        l.getBlock().setType(MaterialWrapper.AIR());
                        l.getBlock().setType(MaterialWrapper.BED());
                        Block block = gameTeam.getBedHead();
                        BlockState bedFoot = block.getState();
                        BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.EAST).getState();
                        bedFoot.setType(MaterialWrapper.BED());
                        bedHead.setType(MaterialWrapper.BED());
                        bedFoot.setRawData((byte) 3);
                        bedHead.setRawData((byte) 11);
                        bedFoot.update(true, false);
                        bedHead.update(true, true);
                    }

                    if (PlayerUtil.getItemInHand(player).getAmount() == 1) {
                        player.getInventory().setItemInHand(null);
                    } else {
                        PlayerUtil.getItemInHand(player).setAmount(PlayerUtil.getItemInHand(player).getAmount() - 1);
                    }

                    gameTeam.setDestroyed(false);
                    gameTeam.setHasBed(true);

                    player.sendMessage("§a使用回春床成功!");
                    gameManager.broadcastSound(SoundWrapper.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                    gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                    gameManager.broadcastMessage(" ");
                    gameManager.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c使用了回春床！");
                    gameManager.broadcastMessage(" ");
                    gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                } else if (material == MaterialWrapper.FIREBALL()) {
                    event.setCancelled(true);
                    if (gamePlayer.isSpectator()) {
                        return;
                    }

                    if (Math.abs(System.currentTimeMillis() - (player.hasMetadata("Game FIREBALL TIMER") ? player.getMetadata("Game FIREBALL TIMER").get(0).asLong() : 0L)) < 1000) {
                        return;
                    }

                    if (PlayerUtil.getItemInHand(player).getAmount() == 1) {
                        PlayerUtil.setItemInHand(player, null);
                    } else {
                        PlayerUtil.getItemInHand(player).setAmount(PlayerUtil.getItemInHand(player).getAmount() - 1);
                    }

                    player.setMetadata("Game FIREBALL TIMER", new FixedMetadataValue(AzuraBedWars.getInstance(), System.currentTimeMillis()));

                    Fireball fireball = player.launchProjectile(Fireball.class);
                    fireball.setVelocity(fireball.getVelocity().multiply(2));
                    fireball.setYield(3.0F);
                    fireball.setBounce(false);
                    fireball.setIsIncendiary(false);
                    fireball.setMetadata("Game FIREBALL", new FixedMetadataValue(AzuraBedWars.getInstance(), player.getUniqueId()));
                } else if (material == MaterialWrapper.WATER_BUCKET()) {
                    for (MapData.RawLocation rawLocation : gameManager.getMapData().getShops()) {
                        if (rawLocation.toLocation().distance(player.getLocation()) <= 5) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    for (GameTeam gameTeam1 : gameManager.getGameTeams()) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && gameManager.getGameState() == GameState.RUNNING) {
            if (event.getRightClicked() instanceof Player && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
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
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().hasMetadata("Shop")) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }
            new ItemShopGUI(player, 0, gameManager).open();
            return;
        }

        if (event.getRightClicked().hasMetadata("Shop2")) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }

            new TeamShopGUI(player, gameManager).open();
        }
    }
}
