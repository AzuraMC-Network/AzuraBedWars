package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
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

public class EggBridgeListener implements Listener {

    private static final Map<Egg, EggBridgeHandler> bridges = new HashMap<>();
    public static List<UUID> inBridgeCooldown = new ArrayList<>();

    private static final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        // 游戏运行判断
        if (gameManager.getGameState() != GameState.RUNNING) {
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


        // 在搭桥蛋 3 秒冷却中
        if (inBridgeCooldown.contains(shooter.getUniqueId())) {
            shooter.sendMessage(ChatColorUtil.color("&c搭桥蛋冷却中！"));
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // 存进生效的搭桥蛋列表
        bridges.put(egg, new EggBridgeHandler(AzuraBedWars.getInstance(), shooter, egg, GamePlayer.get(shooter.getUniqueId()).getGameTeam().getTeamColor()));
        // 创建 3 秒冷却时间
        if (!inBridgeCooldown.contains(shooter.getUniqueId())) {
            inBridgeCooldown.add(shooter.getUniqueId());
        }
        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> inBridgeCooldown.remove(shooter.getUniqueId()), 60L);

    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Egg) {
            removeEgg((Egg) event.getEntity());
        }
    }

    /**
     * 从生效的搭桥蛋列表移除搭桥蛋
     */
    public static void removeEgg(Egg egg) {
        if (bridges.containsKey(egg)) {
            if (bridges.get(egg) != null) {
                bridges.get(egg).cancel();
            }
            bridges.remove(egg);
        }
    }
}
