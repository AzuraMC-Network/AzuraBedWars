package cc.azuramc.bedwars.listeners.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.phase.GameState;
import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.game.data.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.utils.world.MapUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private static final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (gameManager.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING) {
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
                BedBreakHandler.handleBedBreak(event, player, block, gamePlayer, gameTeam);
                return;
            }

            // 检查区域保护和玩家放置的方块
            if (gameManager.getMapData().hasRegion(block.getLocation()) || gameManager.getBlocks().contains(block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
