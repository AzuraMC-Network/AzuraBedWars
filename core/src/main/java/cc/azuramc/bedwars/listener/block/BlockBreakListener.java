package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.MapUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author an5w1r@163.com
 */
public class BlockBreakListener implements Listener {

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer());
        if (GAME_MANAGER.getGameState() != GameState.RUNNING || gamePlayer == null
                || gamePlayer.getGameTeam() == null || gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if (MapUtil.isBedBlock(block)) {
            BedBreakHandler.handleBedBreak(event, block, gamePlayer, gamePlayer.getGameTeam());
            return;
        }

        if (GAME_MANAGER.getMapData().isOutsideRegion(block.getLocation()) || GAME_MANAGER.getBlocksLocation().contains(block.getLocation())) {
            event.setCancelled(true);
        }
    }
}
