package cc.azuramc.bedwars.utils.gui;

import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
public class GUIActionRunnable extends BukkitRunnable {
    private InventoryClickEvent event;

    @Override
    public void run() {

    }
}
