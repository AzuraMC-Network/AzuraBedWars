package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.util.MapUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
        if (GAME_MANAGER.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (GAME_MANAGER.getGameState() == GameState.RUNNING) {
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

            // 处理床方块破坏
            if (MapUtil.isBedBlock(block)) {
                BedBreakHandler.handleBedBreak(event, block, gamePlayer, gameTeam);
                return;
            }

            // 检查区域保护和玩家放置的方块
            if (GAME_MANAGER.getMapData().hasRegion(block.getLocation()) || GAME_MANAGER.getBlocksLocation().contains(block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
