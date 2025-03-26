package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.Bukkit;

public class ChunkListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!game.getMapData().chunkIsInRegion(unload.getChunk().getX(), unload.getChunk().getZ())) {
            return;
        }

        try {
            unload.getClass().getMethod("setCancelled", boolean.class).invoke(unload, true);
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法取消区块卸载事件: " + e.getMessage());
        }
    }
}
