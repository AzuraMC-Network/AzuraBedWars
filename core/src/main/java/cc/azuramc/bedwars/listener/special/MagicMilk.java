package cc.azuramc.bedwars.listener.special;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MagicMilk implements Listener {

    private static final Set<UUID> protectedPlayers = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onDrinkMilk(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // 判断是否喝的是牛奶
        if (event.getItem().getType() != Material.MILK_BUCKET) return;

        UUID uuid = player.getUniqueId();

        // 添加到保护列表
        protectedPlayers.add(uuid);
        player.sendMessage("§a你获得了 30 秒的陷阱免疫效果！");

        // 30秒后移除
        new BukkitRunnable() {
            @Override
            public void run() {
                protectedPlayers.remove(uuid);
                player.sendMessage("§c你的陷阱免疫效果已消失！");
            }
        }.runTaskLater(JavaPlugin.getProvidingPlugin(MagicMilk.class), 20 * 30); // 30s
    }

    public static boolean isTrapImmune(UUID uuid) {
        return protectedPlayers.contains(uuid);
    }
}
