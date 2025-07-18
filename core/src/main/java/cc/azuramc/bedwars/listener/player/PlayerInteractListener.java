package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.gui.ModeSelectionGUI;
import cc.azuramc.bedwars.listener.world.FireballHandler;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.gui.SpectatorCompassGUI;
import cc.azuramc.bedwars.spectator.gui.SpectatorSettingGUI;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author an5w1r@163.com
 */
public class PlayerInteractListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    private final String fireballCooldownMetadata = "GAME_FIREBALL_TIMER";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Material interactingMaterial = event.getMaterial();

        if (gameManager.getGameState() == GameState.WAITING) {
            handleWaitingState(event, gamePlayer, interactingMaterial);
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING) {
            handleRunningState(event, gamePlayer, interactingMaterial);
        }
    }
    
    /**
     * 处理等待状态下的交互事件
     */
    private void handleWaitingState(PlayerInteractEvent event, GamePlayer gamePlayer, Material interactingMaterial) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            switch (interactingMaterial) {
                case PAPER:
                    new ModeSelectionGUI(gamePlayer).open();
                    break;
                case SLIME_BALL:
                    //TODO 回大厅
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * 处理游戏运行状态下的交互事件
     */
    private void handleRunningState(PlayerInteractEvent event, GamePlayer gamePlayer, Material interactingMaterial) {
        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (handleRightClickBlock(event, gamePlayer)) {
            return;
        }

        if (handleSpectatorCompassNavigation(event, gamePlayer, interactingMaterial)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleRightClickAction(event, gamePlayer, gameTeam);
        }
    }
    
    /**
     * 处理右键点击方块的情况
     * @return 如果事件被处理并应该结束，返回true
     */
    private boolean handleRightClickBlock(PlayerInteractEvent event, GamePlayer gamePlayer) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }
        
        // 如果是旁观者点击方块
        if (gamePlayer.isSpectator() && event.getClickedBlock() != null) {
            event.setCancelled(true);
            return true;
        }
        
        // 处理床方块的特殊点击
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("BED")) {
            if (event.getPlayer().isSneaking()) {
                ItemStack item = PlayerUtil.getItemInHand(event.getPlayer());
                if (item != null && item.getType().isBlock()) {
                    return false;
                }
            }
            
            event.setCancelled(true);
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理旁观者使用指南针的导航功能
     * @return 如果事件被处理并应该结束，返回true
     */
    private boolean handleSpectatorCompassNavigation(PlayerInteractEvent event, GamePlayer gamePlayer, Material material) {
        // 检查是否为左键点击动作
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR || 
                              event.getAction() == Action.LEFT_CLICK_BLOCK;
        
        // 检查是否有旁观目标且物品是指南针
        boolean hasSpectatorTargetWithCompass = gamePlayer.getSpectatorTarget() != null && 
                                                material == XMaterial.COMPASS.get();
        
        // 如果同时满足上述条件，执行传送
        if (isLeftClick && hasSpectatorTargetWithCompass) {
            gamePlayer.getSpectatorTarget().tp();
            return true;
        }
        return false;
    }
    
    /**
     * 处理右键点击动作
     */
    private void handleRightClickAction(PlayerInteractEvent event, GamePlayer gamePlayer, GameTeam gameTeam) {

        Material material = event.getMaterial();
        
        if (material == XMaterial.COMPASS.get()) {
            handleCompassInteraction(event, gamePlayer);
        } else if (material == XMaterial.COMPARATOR.get()) {
            new SpectatorSettingGUI(gamePlayer).open();
        } else if (material == XMaterial.PAPER.get()) {
            handlePaperInteraction(event, gamePlayer);
        } else if (material == XMaterial.SLIME_BALL.get()) {
            handleSlimeBallInteraction(event);
        } else if (material.name().toUpperCase().contains("BED")) {
            handleBedInteraction(event, gamePlayer, gameTeam);
        } else if (material == XMaterial.FIRE_CHARGE.get()) {
            handleFireballInteraction(event, gamePlayer);
        } else if (material.name().toUpperCase().contains("WATER_BUCKIT")) {
            handleWaterBucketInteraction(event, gamePlayer);
        }
    }
    
    /**
     * 处理指南针交互
     */
    private void handleCompassInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (!gamePlayer.isSpectator()) {
            return;
        }
        new SpectatorCompassGUI(gamePlayer).open();
    }
    
    /**
     * 处理纸交互
     */
    private void handlePaperInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        Bukkit.dispatchCommand(gamePlayer.getPlayer(), "azurabedwars nextgame");
    }
    
    /**
     * 处理史莱姆球交互
     */
    private void handleSlimeBallInteraction(PlayerInteractEvent event) {
        event.setCancelled(true);
        // back to lobby
    }
    
    /**
     * 处理床交互（回春床功能）
     */
    private void handleBedInteraction(PlayerInteractEvent event, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }

        // 验证能否使用回春床
        if (!canUseRejuvenationBed(gamePlayer, gameTeam)) {
            return;
        }

        // 放置床
        placeBedForTeam(gameTeam);

        // 减少物品
        reduceItemInHand(gamePlayer);

        // 更新队伍状态
        gameTeam.setDestroyed(false);
        gameTeam.setHasBed(true);

        // 广播消息
        announceRejuvenationBedUsed(gamePlayer, gameTeam);
    }
    
    /**
     * 检查玩家是否可以使用回春床
     */
    private boolean canUseRejuvenationBed(GamePlayer gamePlayer, GameTeam gameTeam) {
        int priority = gameManager.getGameEventManager().currentEvent().getPriority();
        int leftTime = gameManager.getGameEventManager().getLeftTime();
        
        // 检查游戏时间是否已超过10分钟
        boolean isTimeExceeded = priority > 2 || (priority == 2 && leftTime <= 120);
        if (isTimeExceeded) {
            gamePlayer.sendMessage("§c开局已超过10分钟.");
            return false;
        }

        if (gameTeam.isHasBed()) {
            gamePlayer.sendMessage("§c已使用过回春床了");
            return false;
        }

        if (!gameTeam.isDestroyed()) {
            gamePlayer.sendMessage("§c床仍然存在 无法使用回春床");
            return false;
        }

        if (gamePlayer.getPlayer().getLocation().distance(gameTeam.getSpawnLocation()) > 18) {
            gamePlayer.sendMessage("§c请靠近出生点使用!");
            return false;
        }
        
        return true;
    }
    
    /**
     * 根据队伍的床朝向放置床
     */
    private void placeBedForTeam(GameTeam gameTeam) {
        BlockFace face = gameTeam.getBedFace();
        LoggerUtil.debug("PlayerInteractListener$placeBedForTeam | bed face: " + face);
        Location bedHeadLocation = gameTeam.getBedHead().getLocation();
        LoggerUtil.debug("PlayerInteractListener$placeBedForTeam | bed head loc: " + bedHeadLocation);
        bedHeadLocation.getBlock().setType(Material.AIR);
        bedHeadLocation.getBlock().setType(XMaterial.RED_BED.get());
        Block block = gameTeam.getBedHead();
        
        BlockState bedFoot = block.getState();
        BlockState bedHead;
        
        switch (face) {
            case NORTH:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.SOUTH).getState();
                bedFoot.setType(XMaterial.RED_BED.get());
                bedHead.setType(XMaterial.RED_BED.get());
                bedFoot.setRawData((byte) 0);
                bedHead.setRawData((byte) 8);
                break;
            case EAST:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.WEST).getState();
                bedFoot.setType(XMaterial.RED_BED.get());
                bedHead.setType(XMaterial.RED_BED.get());
                bedFoot.setRawData((byte) 1);
                bedHead.setRawData((byte) 9);
                break;
            case SOUTH:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.NORTH).getState();
                bedFoot.setType(XMaterial.RED_BED.get());
                bedHead.setType(XMaterial.RED_BED.get());
                bedFoot.setRawData((byte) 2);
                bedHead.setRawData((byte) 10);
                break;
            case WEST:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.EAST).getState();
                bedFoot.setType(XMaterial.RED_BED.get());
                bedHead.setType(XMaterial.RED_BED.get());
                bedFoot.setRawData((byte) 3);
                bedHead.setRawData((byte) 11);
                break;
            default:
                return;
        }
        
        bedFoot.update(true, false);
        bedHead.update(true, true);
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
    }
    
    /**
     * 广播使用回春床的消息
     */
    private void announceRejuvenationBedUsed(GamePlayer gamePlayer, GameTeam gameTeam) {
        gamePlayer.sendMessage("§a使用回春床成功!");
        gameManager.broadcastSound(XSound.ENTITY_ENDER_DRAGON_HURT.get(), 10, 10);
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c使用了回春床！");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
    }
    
    /**
     * 处理火球交互
     */
    private void handleFireballInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }

        // 检查冷却时间
        long lastFireballTime = getLastFireballTime(gamePlayer);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = 1000;
        
        if (currentTime - lastFireballTime < cooldownTime) {
            return;
        }

        // 减少物品
        reduceItemInHand(gamePlayer);

        // 设置冷却时间
        gamePlayer.getPlayer().setMetadata(fireballCooldownMetadata, new FixedMetadataValue(AzuraBedWars.getInstance(), currentTime));

        // 发射火球
        launchFireball(gamePlayer);
    }
    
    /**
     * 获取玩家上次使用火球的时间
     * @return 返回上次使用时间的时间戳，如果没有使用过返回0
     */
    private long getLastFireballTime(GamePlayer gamePlayer) {
        Player player  = gamePlayer.getPlayer();
        if (player.hasMetadata(fireballCooldownMetadata)) {
            return player.getMetadata(fireballCooldownMetadata).getFirst().asLong();
        }
        return 0L;
    }
    
    /**
     * 发射火球
     */
    private void launchFireball(GamePlayer gamePlayer) {
        Fireball fireball = gamePlayer.getPlayer().launchProjectile(Fireball.class);
        fireball.setVelocity(fireball.getVelocity().multiply(2));
        fireball.setYield(3.0F);
        fireball.setBounce(false);
        fireball.setIsIncendiary(false);
        fireball.setMetadata(FireballHandler.FIREBALL_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), gamePlayer.getUuid()));
    }
    
    /**
     * 处理水桶交互
     */
    private void handleWaterBucketInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        // 检查是否靠近商店
        for (MapData.RawLocation rawLocation : gameManager.getMapData().getShops()) {
            if (rawLocation.toLocation().distance(gamePlayer.getPlayer().getLocation()) <= 5) {
                event.setCancelled(true);
                return;
            }
        }

        // 检查是否靠近出生点
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            if (gameTeam.getSpawnLocation().distance(gamePlayer.getPlayer().getLocation()) <= 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(),
                () -> player.getInventory().removeItem(XMaterial.BUCKET.parseItem()), 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && gameManager.getGameState() == GameState.RUNNING) {
            handleSpectatorEntityInteraction(event, gamePlayer);
        }
    }

    /**
     * 处理旁观者与实体的交互
     */
    private void handleSpectatorEntityInteraction(PlayerInteractAtEntityEvent event, GamePlayer gamePlayer) {
        if (event.getRightClicked() instanceof Player targetPlayer && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRST_PERSON)) {
            event.setCancelled(true);

            if (GamePlayer.get(targetPlayer.getUniqueId()).isSpectator()) {
                return;
            }

            enableFirstPersonSpectating(gamePlayer, event.getPlayer(), targetPlayer);
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * 启用第一人称旁观模式
     */
    private void enableFirstPersonSpectating(GamePlayer gamePlayer, Player spectator, Player target) {
        gamePlayer.sendTitle("§a正在旁观§7" + target.getName(), "§a点击左键打开菜单  §c按Shift键退出", 0, 20, 0);
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setSpectatorTarget(target);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().hasMetadata("Shop")) {
            handleItemShopInteraction(event, gamePlayer);
            return;
        }

        if (event.getRightClicked().hasMetadata("Shop2")) {
            handleTeamShopInteraction(event, gamePlayer);
        }
    }
    
    /**
     * 处理物品商店交互
     */
    private void handleItemShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new ItemShopGUI(gamePlayer, 0, gameManager).open();
    }
    
    /**
     * 处理队伍商店交互
     */
    private void handleTeamShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new TeamShopGUI(gamePlayer, gameManager).open();
    }
}