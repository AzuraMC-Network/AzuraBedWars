package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author An5w1r@163.com
 */
public class PlayerInvisibilityListener implements Listener {

    private AzuraBedWars plugin;
    private static final Map<GamePlayer, Boolean> playerInvisibleState = new HashMap<>();
    private static final Map<GamePlayer, BukkitRunnable> invisibilityTasks = new HashMap<>();

    public PlayerInvisibilityListener(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGameManager().getGameState() != GameState.RUNNING) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player);

        if (Objects.requireNonNull(event.getItem().getItemMeta()).getDisplayName().contains("隐身")) {
            LoggerUtil.debug("PlayerInvisibilityListener$onDrink | invisible player is " + player.getName());

            // 如果玩家已经隐身，取消之前的任务
            if (playerInvisibleState.containsKey(gamePlayer) &&
                    playerInvisibleState.get(gamePlayer)) {
                cancelInvisibilityTask(gamePlayer);
            }

            // 隐藏装备
            plugin.getNmsAccess().hideArmor(gamePlayer, GamePlayer.getGamePlayers());
            playerInvisibleState.put(gamePlayer, true);

            BukkitRunnable invisibilityTask = new BukkitRunnable() {
                @Override
                public void run() {
                    endInvisibility(gamePlayer);
                }
            };

            // 30秒 = 30 * 20 ticks
            invisibilityTask.runTaskLater(plugin, 30 * 20);
            invisibilityTasks.put(gamePlayer, invisibilityTask);
        }
    }

    /**
     * 结束玩家隐身状态
     */
    private static void endInvisibility(GamePlayer gamePlayer) {
        if (gamePlayer != null && gamePlayer.isOnline()) {
            AzuraBedWars.getInstance().getNmsAccess().showArmor(gamePlayer, GamePlayer.getGamePlayers());
            LoggerUtil.debug("PlayerInvisibilityListener$endInvisibility | player " + gamePlayer.getName() + " invisibility ended");
        }

        if (gamePlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            gamePlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        playerInvisibleState.remove(gamePlayer);
        invisibilityTasks.remove(gamePlayer);
    }

    /**
     * 取消隐身任务
     */
    private static void cancelInvisibilityTask(GamePlayer gamePlayer) {
        BukkitRunnable task = invisibilityTasks.get(gamePlayer);
        if (task != null) {
            task.cancel();
            invisibilityTasks.remove(gamePlayer);
        }
    }

    /**
     * 清理玩家数据（玩家离开时调用）
     */
    public static void cleanupPlayer(GamePlayer gamePlayer) {
        cancelInvisibilityTask(gamePlayer);
        playerInvisibleState.remove(gamePlayer);
    }

    /**
     * 检查玩家是否处于隐身状态
     */
    public static boolean isPlayerInvisible(GamePlayer gamePlayer) {
        return playerInvisibleState.getOrDefault(gamePlayer, false);
    }

    /**
     * 强制结束玩家隐身（例如玩家死亡时）
     */
    public static void forceEndInvisibility(GamePlayer gamePlayer) {
        if (isPlayerInvisible(gamePlayer)) {
            cancelInvisibilityTask(gamePlayer);
            endInvisibility(gamePlayer);
        }
    }
}