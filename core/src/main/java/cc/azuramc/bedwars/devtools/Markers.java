package cc.azuramc.bedwars.devtools;

import cc.azuramc.bedwars.AzuraBedWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Floating name tags at map points, so the admin can see in game what the AI configured
 *
 * @author an5w1r@163.com
 */
public final class Markers {

    /** Also finds tags left behind by a crash, the stands are saved with the world */
    private static final String PREFIX = "§3[AI] §f";

    private final AzuraBedWars plugin;
    @Nullable
    private BukkitTask expiry;

    Markers(AzuraBedWars plugin) {
        this.plugin = plugin;
    }

    /**
     * Must run on the main thread
     */
    public void show(List<Location> locations, List<String> labels, int seconds) {
        clear();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setCustomName(PREFIX + labels.get(i));
            stand.setCustomNameVisible(true);
        }
        expiry = Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), this::clear, seconds * 20L);
    }

    /**
     * Must run on the main thread
     */
    public int clear() {
        if (expiry != null) {
            expiry.cancel();
            expiry = null;
        }
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (stand.getCustomName() != null && stand.getCustomName().startsWith(PREFIX)) {
                    stand.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
}
