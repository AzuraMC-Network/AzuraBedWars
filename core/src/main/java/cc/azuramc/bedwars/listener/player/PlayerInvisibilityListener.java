package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class PlayerInvisibilityListener implements Listener {

    private final AzuraBedWars plugin;
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

        ItemStack itemStack = event.getItem();
        if (itemStack.getType() == Material.AIR || itemStack.getItemMeta() == null ||  itemStack.getItemMeta() == null) {
            return;
        }

        String itemName =  itemStack.getItemMeta().getDisplayName();
        if (itemName == null || itemName.isEmpty()) {
            return;
        }

        if (itemName.contains("隐身")) {
            LoggerUtil.debug("PlayerInvisibilityListener$onDrink | invisible player is " + player.getName());

            // 如果玩家已经隐身，取消之前的任务
            if (gamePlayer.isInvisible()) {
                cancelInvisibilityTask(gamePlayer);
            }

            // 隐藏装备
            plugin.getNmsAccess().hideArmor(gamePlayer, GamePlayer.getGamePlayers());
            gamePlayer.setInvisible(true);

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
        if (gamePlayer == null) {
            return;
        }
        if (gamePlayer.isOnline()) {
            AzuraBedWars.getInstance().getNmsAccess().showArmor(gamePlayer, GamePlayer.getGamePlayers());
            LoggerUtil.debug("PlayerInvisibilityListener$endInvisibility | player " + gamePlayer.getName() + " invisibility ended");
        }

        if (gamePlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            gamePlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        gamePlayer.setInvisible(false);
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
        gamePlayer.setInvisible(false);
    }

    /**
     * 强制结束玩家隐身（例如玩家死亡时）
     */
    public static void forceEndInvisibility(GamePlayer gamePlayer) {
        if (gamePlayer.isInvisible()) {
            cancelInvisibilityTask(gamePlayer);
            endInvisibility(gamePlayer);
        }
    }
}