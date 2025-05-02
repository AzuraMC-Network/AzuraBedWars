package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.ChatColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.*;

/**
 * @author an5w1r@163.com
 */
public class EggBridgeListener implements Listener {

    private static final ItemConfig.EggBridge CONFIG = AzuraBedWars.getInstance().getItemConfig().getEggBridge();

    private static final int EGG_COOLDOWN_SECONDS = CONFIG.getEggCooldownSeconds();
    private static final String EGG_COOLDOWN_MESSAGE = CONFIG.getEggCooldownMessage();

    private static final Map<Egg, EggBridgeHandler> BRIDGES = new HashMap<>();

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        // 游戏运行判断
        if (GAME_MANAGER.getGameState() != GameState.RUNNING) {
            return;
        }

        // 投掷物是否为鸡蛋判断
        if (!(event.getEntity() instanceof Egg egg)) {
            return;
        }

        // 仅为玩家
        if (!(egg.getShooter() instanceof Player shooter)) {
            return;
        }

        // 仅为游戏玩家
        GamePlayer gamePlayer = GamePlayer.get(shooter.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 搭桥蛋冷却中
        if (gamePlayer.isEggBridgeCooldown()) {
            shooter.sendMessage(ChatColorUtil.color(EGG_COOLDOWN_MESSAGE));
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // 存进生效的搭桥蛋列表
        BRIDGES.put(egg, new EggBridgeHandler(AzuraBedWars.getInstance(), shooter, egg, GamePlayer.get(shooter.getUniqueId()).getGameTeam().getTeamColor()));

        // 创建冷却
        if (!gamePlayer.isEggBridgeCooldown()) {
            gamePlayer.setEggBridgeCooldown(true);
            Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> gamePlayer.setEggBridgeCooldown(false), EGG_COOLDOWN_SECONDS * 20L);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        // 取消搭桥蛋命中实体
        if (event.getEntity() instanceof Egg) {
            removeEgg((Egg) event.getEntity());
        }
    }

    /**
     * 移除生效中的搭桥蛋
     */
    public static void removeEgg(Egg egg) {
        if (BRIDGES.containsKey(egg)) {
            if (BRIDGES.get(egg) != null) {
                BRIDGES.get(egg).cancel();
            }
            BRIDGES.remove(egg);
        }
    }
}
