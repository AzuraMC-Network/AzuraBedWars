package cc.azuramc.bedwars.listener.world;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ChunkListener implements Listener {
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private static final boolean SUPPORTS_DIRECT_CANCEL;
    private static final boolean SUPPORTS_FORCE_LOADED;
    private static Method SET_FORCE_LOADED_METHOD = null;
    private static final Set<String> FORCE_LOADED_CHUNKS = new HashSet<>();

    static {
        // 检查是否支持直接取消区块卸载事件
        boolean directCancel = false;
        try {
            ChunkUnloadEvent.class.getMethod("setCancelled", boolean.class);
            directCancel = true;
        } catch (NoSuchMethodException ignored) {
            // 旧版本Bukkit不支持直接取消
        }
        SUPPORTS_DIRECT_CANCEL = directCancel;

        // 检查是否支持ForceLoadedChunk API (1.13+)
        boolean supportsForceLoaded = false;
        try {
            SET_FORCE_LOADED_METHOD = Chunk.class.getMethod("setForceLoaded", boolean.class);
            supportsForceLoaded = true;
        } catch (NoSuchMethodException ignored) {
            // 旧版本不支持ForceLoadedChunk API
        }
        SUPPORTS_FORCE_LOADED = supportsForceLoaded;
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!gameManager.getMapData().chunkIsInRegion(unload.getChunk().getX(), unload.getChunk().getZ())) {
            return;
        }

        // 首先尝试使用ForceLoadedChunk API
        if (SUPPORTS_FORCE_LOADED) {
            try {
                Chunk chunk = unload.getChunk();
                String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
                
                if (!FORCE_LOADED_CHUNKS.contains(chunkKey)) {
                    SET_FORCE_LOADED_METHOD.invoke(chunk, true);
                    FORCE_LOADED_CHUNKS.add(chunkKey);
                    Bukkit.getLogger().info("已强制加载区块: " + chunkKey);
                    return;
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("无法使用ForceLoadedChunk API: " + e.getMessage());
            }
        }

        // 如果ForceLoadedChunk失败，回退到旧方法
        cancelChunkUnload(unload);
    }

    /**
     * 兼容不同版本取消区块卸载
     * @param event 区块卸载事件
     */
    private void cancelChunkUnload(ChunkUnloadEvent event) {
        if (SUPPORTS_DIRECT_CANCEL) {
            try {
                event.getClass().getMethod("setCancelled", boolean.class).invoke(event, true);
            } catch (Exception e) {
                Bukkit.getLogger().warning("无法直接取消区块卸载事件: " + e.getMessage());
            }
        }
    }

    /**
     * 释放强制加载的区块
     * 在游戏结束时调用此方法
     */
    public static void releaseForceLoadedChunks() {
        if (!SUPPORTS_FORCE_LOADED) {
            return;
        }

        for (String chunkKey : FORCE_LOADED_CHUNKS) {
            try {
                String[] parts = chunkKey.split(":");
                World world = Bukkit.getWorld(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                
                if (world != null && world.isChunkLoaded(x, z)) {
                    Chunk chunk = world.getChunkAt(x, z);
                    SET_FORCE_LOADED_METHOD.invoke(chunk, false);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("释放强制加载区块失败: " + e.getMessage());
            }
        }
        
        FORCE_LOADED_CHUNKS.clear();
    }
}
