package cc.azuramc.bedwars.listener.setup;

import cc.azuramc.bedwars.command.admin.MapCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class SetupItemListener implements Listener {

    private final MapCommand mapCommand = new MapCommand();
    private final Map<UUID, String> editingMaps = new HashMap<>();

    public void start(Player player, String mapName) {
        editingMaps.put(player.getUniqueId(), mapName);
        player.getInventory().clear();
        for (SetupItem item : SetupItem.values()) {
            player.getInventory().setItem(item.ordinal(), item.createItem());
        }
    }

    /**
     * @return false when the player was not editing a map
     */
    public boolean stop(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (SetupItem.of(item) != null) {
                player.getInventory().remove(item);
            }
        }
        return editingMaps.remove(player.getUniqueId()) != null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        String mapName = editingMaps.get(event.getPlayer().getUniqueId());
        SetupItem item = SetupItem.of(event.getItem());
        if (mapName == null || item == null) {
            return;
        }
        event.setCancelled(true);
        item.run(mapCommand, event.getPlayer(), mapName);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        editingMaps.remove(event.getPlayer().getUniqueId());
    }
}
