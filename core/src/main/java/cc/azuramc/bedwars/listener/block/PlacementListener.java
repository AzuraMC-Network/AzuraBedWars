package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.*;
import cc.azuramc.bedwars.game.item.special.SpecialItems;
import cc.azuramc.bedwars.popuptower.impl.TowerEast;
import cc.azuramc.bedwars.popuptower.impl.TowerNorth;
import cc.azuramc.bedwars.popuptower.impl.TowerSouth;
import cc.azuramc.bedwars.popuptower.impl.TowerWest;
import cc.azuramc.bedwars.util.MapUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;


/**
 * @author an5w1r@163.com
 */
public class PlacementListener implements Listener {

    private static final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        Block block = event.getBlock();

        if (gameManager.getGameState() != GameState.RUNNING || gamePlayer == null
                || gamePlayer.getGameTeam() == null || gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (MapUtil.isBedBlock(block)) {
            event.setCancelled(true);
            return;
        }

        if (MapUtil.isProtectedArea(block.getLocation())) {
            gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getGame().getCannotPlace());
            event.setCancelled(true);
            return;
        }

        if (block.getLocation().getY() > gameManager.getBuildLimitHeight()) {
            gamePlayer.sendMessage(AzuraBedWars.getInstance().getLang().getGame().getCannotPlace());
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItemInHand();
        if (SpecialItems.is(item, SpecialItems.TNT)) {
            TNTPlacementHandler.handleTNTPlacement(event, player);
            return;
        }

        if (SpecialItems.is(item, SpecialItems.SPEED_WOOL)) {
            handleSpeedWoolPlacement(event, player, item);
        }

        if (SpecialItems.is(item, SpecialItems.POPUP_TOWER)) {
            handlePopUpTowerPlacement(event, player);
        }
    }

    private void handleSpeedWoolPlacement(BlockPlaceEvent event, Player player, ItemStack item) {

        // keep the player from getting stuck inside the bridge
        Block block = event.getBlock();
        if (block.getY() != event.getBlockAgainst().getY()) {
            if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)),
                    Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                return;
            }
        }

        BlockFace blockFace = event.getBlockAgainst().getFace(block);

        SpeedWoolHandler.startSpeedWoolTask(block, blockFace, item);
    }

    private void handlePopUpTowerPlacement(BlockPlaceEvent event, Player player) {
        event.setCancelled(true);
        Location loc = event.getBlock().getLocation();
        GamePlayer gamePlayer = GamePlayer.get(player);
        if (gamePlayer == null) {
            return;
        }
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }
        TeamColor color = gameTeam.getTeamColor();
        double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if (45.0D <= rotation && rotation < 135.0D) {
            new TowerSouth(loc, event.getBlockPlaced(), color, player);
        } else if (225.0D <= rotation && rotation < 315.0D) {
            new TowerNorth(loc, event.getBlockPlaced(), color, player);
        } else if (135.0D <= rotation && rotation < 225.0D) {
            new TowerWest(loc, event.getBlockPlaced(), color, player);
        } else if (0.0D <= rotation && rotation < 45.0D) {
            new TowerEast(loc, event.getBlockPlaced(), color, player);
        } else if (315.0D <= rotation && rotation < 360.0D) {
            new TowerEast(loc, event.getBlockPlaced(), color, player);
        }
    }
}
