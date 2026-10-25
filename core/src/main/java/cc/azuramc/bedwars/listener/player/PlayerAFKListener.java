package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
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

    private static final SettingsConfig settingsConfig = AzuraBedWars.getInstance().getSettingsConfig();

    private static final Map<UUID, Long> AFK_LAST_MOVEMENT = new HashMap<>();
    private static int checkAFKTask;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        AFK_LAST_MOVEMENT.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearAfk(event.getPlayer().getUniqueId());
    }

    public static void startCheckAFKTask() {
        checkAFKTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AzuraBedWars.getInstance().getPlugin(), () -> {
            long currentTime = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                GamePlayer gamePlayer = GamePlayer.get(uuid);
                long lastMove = AFK_LAST_MOVEMENT.getOrDefault(uuid, currentTime);

                if (gamePlayer != null) {
                    gamePlayer.setAfk(currentTime - lastMove >= settingsConfig.getMaxNoMovementTime() * 1000L);
                }
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
