package cc.azuramc.bedwars.listener.world;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.lang.reflect.Method;

/**
 * @author an5w1r@163.com
 */
public class ChunkListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    private static Method setCancelMethod;

    static {
        if (VersionUtil.isLessThan(1, 13)) {
            try {
                setCancelMethod = ChunkUnloadEvent.class.getMethod("setCancelled", boolean.class);
            } catch (NoSuchMethodException e) {
                AzuraBedWars.getInstance().getLogger().warning("无法找到 ChunkUnloadEvent.setCancelled 方法");
            }
        }
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!gameManager.getMapData().chunkIsInRegion(event.getChunk().getX(), event.getChunk().getZ())) {
            return;
        }

        if (VersionUtil.isLessThan(1, 13)) {
            if (setCancelMethod != null) {
                try {
                    setCancelMethod.invoke(event, true);
                } catch (Exception e) {
                    AzuraBedWars.getInstance().getLogger().warning("ChunkListener$onUnload | " + e.getMessage());
                }
            }
            return;
        }

        event.getChunk().setForceLoaded(true);
    }
}
