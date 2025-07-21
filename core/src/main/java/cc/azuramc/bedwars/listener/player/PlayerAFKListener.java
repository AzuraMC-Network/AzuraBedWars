package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author ant1aura@qq.com
 */
public class PlayerAFKListener implements Listener {

    private static final PlayerConfig.AFKCheck CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getAfkCheck();

    private static final int MAX_NO_MOVEMENT_TIME = CONFIG.getMaxNoMovementTime();

    private static final Map<UUID, Long> AFK_LAST_MOVEMENT = new HashMap<>();
    private static int checkAFKTask;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        // 记录玩家上次移动的时间
        AFK_LAST_MOVEMENT.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // 退出时清理挂机状态检测遗留
        clearAfk(event.getPlayer().getUniqueId());
    }

    /**
     * 开始挂机状态检测
     */
    public static void startCheckAFKTask() {
        checkAFKTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AzuraBedWars.getInstance(), () -> {
            long currentTime = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                GamePlayer gamePlayer = GamePlayer.get(uuid);
                long lastMove = AFK_LAST_MOVEMENT.getOrDefault(uuid, currentTime);

                // 大于时间未移动
                gamePlayer.setAfk(currentTime - lastMove >= MAX_NO_MOVEMENT_TIME * 1000L);
            }
        }, 0L, 20L);
    }

    private void clearAfk(UUID uuid) {
        AFK_LAST_MOVEMENT.remove(uuid);
        GamePlayer gamePlayer = GamePlayer.get(uuid);
        if (gamePlayer != null) {
            gamePlayer.setAfk(false);
        }
    }

    public static void stop() {
        GamePlayer.getGamePlayers().forEach(a -> a.setAfk(false));
        AFK_LAST_MOVEMENT.clear();
        Bukkit.getScheduler().cancelTask(checkAFKTask);
    }
}
