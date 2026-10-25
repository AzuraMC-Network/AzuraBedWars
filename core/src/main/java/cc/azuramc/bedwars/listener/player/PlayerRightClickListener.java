package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.spectator.gui.SpectatorCompassGUI;
import cc.azuramc.bedwars.game.spectator.gui.SpectatorSettingGUI;
import cc.azuramc.bedwars.util.BungeeUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.MapUtil;
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
 * @author an5w1r@163.com
 */
public class PlayerRightClickListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            return;
        }

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (handleRightClickBlock(event, gamePlayer)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleRightClickAction(event, gamePlayer);
        }
    }

    private boolean handleRightClickBlock(PlayerInteractEvent event, GamePlayer gamePlayer) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        if (gamePlayer.isSpectator() && event.getClickedBlock() != null) {
            event.setCancelled(true);
            return true;
        }

        if (event.getClickedBlock() != null && MapUtil.isBedBlock(event.getClickedBlock())) {
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

    private void handleRightClickAction(PlayerInteractEvent event, GamePlayer gamePlayer) {

        Material material = event.getMaterial();

        if (material == XMaterial.COMPASS.get()) {
            handleCompassInteraction(event, gamePlayer);
        } else if (material == XMaterial.COMPARATOR.get()) {
            new SpectatorSettingGUI(gamePlayer).open();
        } else if (material == XMaterial.PAPER.get()) {
            handlePaperInteraction(event, gamePlayer);
        } else if (material == XMaterial.SLIME_BALL.get()) {
            handleSlimeBallInteraction(event);
        } else if (MapUtil.isBed(material) && gamePlayer.getGameTeam() != null) {
            handleBedInteraction(event, gamePlayer, gamePlayer.getGameTeam());
        }
    }

    private void handleCompassInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (!gamePlayer.isSpectator()) {
            return;
        }
        new SpectatorCompassGUI(gamePlayer).open();
    }

    private void handlePaperInteraction(PlayerInteractEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        String command = AzuraBedWars.getInstance().getSettingsConfig().getPlayAgainCommand();
        Bukkit.dispatchCommand(gamePlayer.getPlayer(), command.startsWith("/") ? command.substring(1) : command);
    }

    private void handleSlimeBallInteraction(PlayerInteractEvent event) {
        event.setCancelled(true);
        BungeeUtil.connectToLobby(GamePlayer.get(event.getPlayer()));
    }

    private void handleBedInteraction(PlayerInteractEvent event, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }

        if (!canUseRejuvenationBed(gamePlayer, gameTeam)) {
            return;
        }

        placeBedForTeam(gameTeam);

        reduceItemInHand(gamePlayer);

        gameTeam.setDestroyed(false);
        gameTeam.setRejuvenated(true);

        announceRejuvenationBedUsed(gamePlayer, gameTeam);
    }

    private void reduceItemInHand(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (PlayerUtil.getItemInHand(player).getAmount() == 1) {
            PlayerUtil.setItemInHand(player, null);
        } else {
            PlayerUtil.getItemInHand(player).setAmount(PlayerUtil.getItemInHand(player).getAmount() - 1);
        }
    }

    private void announceRejuvenationBedUsed(GamePlayer gamePlayer, GameTeam gameTeam) {
        gamePlayer.sendMessage("§a使用回春床成功!");
        gameManager.broadcastSound(XSound.ENTITY_ENDER_DRAGON_HURT.get(), 10, 10);
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c使用了回春床！");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
    }


    private boolean canUseRejuvenationBed(GamePlayer gamePlayer, GameTeam gameTeam) {
        if (gameManager.getTimeline().getGameSeconds() >= 600) {
            gamePlayer.sendMessage("§c开局已超过10分钟.");
            return false;
        }

        if (gameTeam.isRejuvenated()) {
            gamePlayer.sendMessage("§c已使用过回春床了");
            return false;
        }

        if (!gameTeam.isDestroyed()) {
            gamePlayer.sendMessage("§c床仍然存在 无法使用回春床");
            return false;
        }

        if (gameTeam.getBedHead() == null) {
            gamePlayer.sendMessage("§c地图没有为你的队伍设置床");
            return false;
        }

        if (gamePlayer.getPlayer().getLocation().distance(gameTeam.getSpawnLocation()) > 18) {
            gamePlayer.sendMessage("§c请靠近出生点使用!");
            return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void placeBedForTeam(GameTeam gameTeam) {
        BlockFace face = gameTeam.getBedFace();
        if (face == null) {
            return;
        }
        LoggerUtil.debug("PlayerInteractListener$placeBedForTeam | bed face: " + face);
        if (gameTeam.getBedHead() == null) {
            return;
        }
        Location bedHeadLocation = gameTeam.getBedHead().getLocation();

        LoggerUtil.debug("PlayerInteractListener$placeBedForTeam | bed head loc: " + bedHeadLocation);
        bedHeadLocation.getBlock().setType(Material.AIR);

        Material redBed = XMaterial.RED_BED.get();
        if (redBed == null) {
            return;
        }
        bedHeadLocation.getBlock().setType(redBed);
        Block block = gameTeam.getBedHead();

        BlockState bedFoot = block.getState();
        BlockState bedHead;

        switch (face) {
            case NORTH:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.SOUTH).getState();
                bedFoot.setType(redBed);
                bedHead.setType(redBed);
                bedFoot.setRawData((byte) 0);
                bedHead.setRawData((byte) 8);
                break;
            case EAST:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.WEST).getState();
                bedFoot.setType(redBed);
                bedHead.setType(redBed);
                bedFoot.setRawData((byte) 1);
                bedHead.setRawData((byte) 9);
                break;
            case SOUTH:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.NORTH).getState();
                bedFoot.setType(redBed);
                bedHead.setType(redBed);
                bedFoot.setRawData((byte) 2);
                bedHead.setRawData((byte) 10);
                break;
            case WEST:
                bedHead = bedFoot.getBlock().getRelative(BlockFace.EAST).getState();
                bedFoot.setType(redBed);
                bedHead.setType(redBed);
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
