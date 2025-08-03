package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class MagicMilk implements Listener {

    @EventHandler
    public void onDrinkMilk(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        GamePlayer gamePlayer = GamePlayer.get(player);

        // 判断是否喝的是牛奶
        if (event.getItem().getType() != Material.MILK_BUCKET) return;

        UUID uuid = player.getUniqueId();

        gamePlayer.setprotectedPlayer(true);

        player.sendMessage("§a你获得了 30 秒的陷阱免疫效果！");

        // 30秒后移除
        new BukkitRunnable() {
            @Override
            public void run() {
                gamePlayer.setprotectedPlayer(false);
                Player onlinePlayer = org.bukkit.Bukkit.getPlayer(uuid);
                if (onlinePlayer != null) {
                    onlinePlayer.sendMessage("§c你的陷阱免疫效果已消失！");
                }
            }
        }.runTaskLater(JavaPlugin.getProvidingPlugin(MagicMilk.class), 20 * 30); // 30s
    }
}
