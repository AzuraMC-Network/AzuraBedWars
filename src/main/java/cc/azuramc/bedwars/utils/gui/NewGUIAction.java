package cc.azuramc.bedwars.utils.gui;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;

@Data
@AllArgsConstructor
public class NewGUIAction {
    private int delay;
    private NewGUIActionRunnable runnable;
    private boolean close;

    public interface NewGUIActionRunnable {
        void run(InventoryClickEvent event);
    }
}
