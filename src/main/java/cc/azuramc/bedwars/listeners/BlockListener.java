package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.utils.ActionBarUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.events.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.utils.SoundUtil;
import cc.azuramc.bedwars.utils.Util;
import cc.azuramc.bedwars.utils.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BlockListener implements Listener {
    private final AzuraBedWars main;
    private final Game game;
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Material.valueOf("PLAYER_HEAD");
            newVersion = true;
        } catch (IllegalArgumentException e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }

    public BlockListener(AzuraBedWars main) {
        this.main = main;
        this.game = main.getGame();
    }

    private boolean isBedBlock(Block block) {
        return block.getType().name().endsWith("_BED") || block.getType().name().equals("BED_BLOCK");
    }
    
    private boolean isStainedGlass(Block block) {
        return block.getType().name().contains("STAINED_GLASS");
    }
    
    private void setBlockData(Block block, byte data) {
        try {
            if (!NEW_VERSION) {
                java.lang.reflect.Method setDataMethod = Block.class.getMethod("setData", byte.class);
                setDataMethod.invoke(block, data);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法设置方块数据: " + e.getMessage());
        }
    }

    private ItemStack getItemInHand(Player player) {
        try {
            if (NEW_VERSION) {
                return player.getInventory().getItemInMainHand();
            } else {
                return (ItemStack) player.getClass().getMethod("getItemInHand").invoke(player);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void setItemInHand(Player player, ItemStack item) {
        try {
            if (NEW_VERSION) {
                player.getInventory().setItemInMainHand(item);
            } else {
                player.getClass().getMethod("setItemInHand", ItemStack.class).invoke(player, item);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法设置玩家手中物品: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        Block block = event.getBlock();

        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (block.getType().toString().startsWith("BED")) {
            event.setCancelled(true);
            return;
        }

        if (game.getMapData().hasRegion(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        for (GameTeam gameTeam : game.getGameTeams()) {
            if (gameTeam.getSpawn().distance(block.getLocation()) <= 5) {
                event.setCancelled(true);
                return;
            }
        }

        for (MapData.RawLocation rawLocation : game.getMapData().getDrops()) {
            if (rawLocation.toLocation().distance(block.getLocation()) <= 3) {
                event.setCancelled(true);
                return;
            }
        }

        if (block.getType() == MaterialUtil.TNT()) {
            event.setCancelled(true);
            event.getBlock().setType(MaterialUtil.AIR());

            TNTPrimed tnt = event.getBlock().getWorld().spawn(block.getLocation().add(0.5D, 0.0D, 0.5D), TNTPrimed.class);
            tnt.setVelocity(new Vector(0, 0, 0));

            ItemStack item = getItemInHand(player);
            if (item != null && item.getType() == MaterialUtil.TNT()) {
                if (item.getAmount() == 1) {
                    setItemInHand(player, null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
            }
            return;
        }

        ItemStack item = getItemInHand(player);
        if (item != null && item.getType() == MaterialUtil.WHITE_WOOL() && !item.getEnchantments().isEmpty()) {
            if (Math.abs(System.currentTimeMillis() - (player.hasMetadata("Game BLOCK TIMER") ? player.getMetadata("Game BLOCK TIMER").get(0).asLong() : 0L)) < 1000) {
                event.setCancelled(true);
                return;
            }
            player.setMetadata("Game BLOCK TIMER", new FixedMetadataValue(AzuraBedWars.getInstance(), System.currentTimeMillis()));

            if (block.getY() != event.getBlockAgainst().getY()) {
                if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)), Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                    return;
                }
            }
            BlockFace blockFace = event.getBlockAgainst().getFace(block);

            new BukkitRunnable() {
                int i = 1;

                @Override
                public void run() {
                    if (i > 6) {
                        cancel();
                    }

                    for (GameTeam gameTeam : game.getGameTeams()) {
                        if (gameTeam.getSpawn().distance(block.getRelative(blockFace, i).getLocation()) <= 5) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (AzuraBedWars.getInstance().getGame().getMapData().hasRegion(block.getRelative(blockFace, i).getLocation())) {
                        return;
                    }

                    for (Location location : game.getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
                        if (location.distance(block.getRelative(blockFace, i).getLocation()) <= 3) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    for (Location location : game.getMapData().getDropLocations(MapData.DropType.EMERALD)) {
                        if (location.distance(block.getRelative(blockFace, i).getLocation()) <= 3) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (block.getRelative(blockFace, i).getType() == MaterialUtil.AIR()) {
                        block.getRelative(blockFace, i).setType(item.getType());
                        if (!NEW_VERSION) {
                            setBlockData(block.getRelative(blockFace, i), item.getData().getData());
                        }
                        block.getWorld().playSound(block.getLocation(), SoundUtil.get("STEP_WOOL", "BLOCK_CLOTH_STEP"), 1f, 1f);
                    }

                    i++;
                }
            }.runTaskTimer(AzuraBedWars.getInstance(), 0, 4L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            if (gamePlayer == null) {
                return;
            }

            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                return;
            }

            if (isBedBlock(block)) {
                event.setCancelled(true);

                if (gameTeam.getSpawn().distance(block.getLocation()) <= 18.0D) {
                    player.sendMessage("§c你不能破坏你家的床");
                    return;
                }

                for (GameTeam gameTeam1 : game.getGameTeams()) {
                    if (gameTeam1.getSpawn().distance(block.getLocation()) <= 18.0D) {
                        if (!gameTeam1.isDead()) {
                            Util.dropTargetBlock(block);

                            new BukkitRunnable() {
                                int i = 0;

                                @Override
                                public void run() {
                                    if (i == 5) {
                                        cancel();
                                        return;
                                    }
                                    ActionBarUtil.sendBar(player, "§6+10个金币");
                                    i++;
                                }
                            }.runTaskTimerAsynchronously(AzuraBedWars.getInstance(), 0, 10);
                            player.sendMessage("§6+10个金币 (破坏床)");
                            AzuraBedWars.getInstance().getEcon().depositPlayer(player, 10);

                            game.broadcastSound(SoundUtil.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                            game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                            game.broadcastMessage(" ");
                            game.broadcastMessage("§c§l" + gameTeam1.getName() + " §a的床被 " + gameTeam.getChatColor() + gamePlayer.getDisplayname() + "§a 挖爆!");
                            game.broadcastMessage(" ");
                            game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");

                            game.broadcastTeamTitle(gameTeam1, 1, 20, 1, "§c§l床被摧毁", "§c死亡将无法复活");

                            Bukkit.getPluginManager().callEvent(new BedwarsDestroyBedEvent(player, gameTeam1));

                            gameTeam1.setDestroyPlayer(gamePlayer);
                            gameTeam1.setBedDestroy(true);

                            gamePlayer.getPlayerData().addDestroyedBeds();
                            return;
                        }
                        player.sendMessage("§c此床没有队伍");
                        return;
                    }
                }
            }

            if (game.getMapData().hasRegion(block.getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (game.getBlocks().contains(block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        for (int i = 0; i < event.blockList().size(); i++) {
            Block b = event.blockList().get(i);
            if (AzuraBedWars.getInstance().getGame().getMapData().hasRegion(b.getLocation())) {
                event.setCancelled(true);
                continue;
            }

            if (!isStainedGlass(b) && !isBedBlock(b)) {
                if (!game.getBlocks().contains(b.getLocation())) {
                    event.setCancelled(true);
                    b.setType(MaterialUtil.AIR());
                    
                    if (NEW_VERSION) {
                        b.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, b.getLocation(), 5);
                    } else {
                        b.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 0);
                    }
                    
                    b.getWorld().playSound(b.getLocation(), SoundUtil.get("EXPLODE", "ENTITY_GENERIC_EXPLODE"), 1.0F, 1.0F);
                }
            }
        }

        if (entity instanceof Fireball) {
            Fireball fireball = (Fireball) entity;
            if (!fireball.hasMetadata("Game FIREBALL")) {
                return;
            }
            GamePlayer ownerPlayer = GamePlayer.get((UUID) fireball.getMetadata("Game FIREBALL").get(0).value());

            for (Entity entity1 : entity.getNearbyEntities(4, 3, 4)) {
                if (!(entity1 instanceof Player)) {
                    continue;
                }

                Player player = (Player) entity1;
                GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

                if (fireball.hasMetadata("Game FIREBALL")) {
                    GameTeam gameTeam = ownerPlayer.getGameTeam();
                    if (gameTeam != null && gameTeam.isInTeam(ownerPlayer, gamePlayer)) {
                        continue;
                    }
                }

                player.damage(3);
                gamePlayer.getAssistsMap().setLastDamage(ownerPlayer, System.currentTimeMillis());
                player.setMetadata("FIREBALL PLAYER NOFALL", new FixedMetadataValue(AzuraBedWars.getInstance(), ownerPlayer.getUuid()));
                player.setVelocity(Util.getPosition(player.getLocation(), fireball.getLocation(), 1.5D).multiply(0.5));
            }
        }
        event.setCancelled(true);
    }
}
