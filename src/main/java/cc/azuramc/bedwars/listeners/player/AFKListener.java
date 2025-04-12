package cc.azuramc.bedwars.listeners.player;

import cc.azuramc.bedwars.AzuraBedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKListener implements Listener {

    private static final int MAX_NO_MOVEMENT_TIME = 45;

    public static Map<UUID, Long> afkLastMovement = new HashMap<>();
    public static Map<UUID, Boolean> afk = new HashMap<>();
    public static int checkAFKTask;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 记录玩家上次移动的时间
        afkLastMovement.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // 退出时清理挂机状态检测遗留
        clearAFK(event.getPlayer().getUniqueId());
    }

    /**
     * 开始挂机状态检测
     */
    public static void startCheckAFKTask() {
        checkAFKTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AzuraBedWars.getInstance(), () -> {
            long currentTime = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                long lastMove = afkLastMovement.getOrDefault(uuid, currentTime);

                // 大于时间未移动
                if (currentTime - lastMove >= MAX_NO_MOVEMENT_TIME * 1000L) {
                    afk.put(uuid, true);
                } else {
                    afk.put(uuid, false);
                }
            }
        }, 0L, 20L);
    }

    private void clearAFK(UUID uuid) {
        afkLastMovement.remove(uuid);
        afk.remove(uuid);
    }

    public static void stop() {
        afk.clear();
        afkLastMovement.clear();
        Bukkit.getScheduler().cancelTask(AFKListener.checkAFKTask);
    }
}
