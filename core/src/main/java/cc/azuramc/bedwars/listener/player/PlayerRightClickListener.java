package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.spectator.gui.SpectatorCompassGUI;
import cc.azuramc.bedwars.spectator.gui.SpectatorSettingGUI;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author An5w1r@163.com
 */
public class PlayerRightClickListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (handleRightClickBlock(event, gamePlayer)) {
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

}
