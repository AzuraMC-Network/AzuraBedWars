package cc.azuramc.bedwars.gui.base.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author an5w1r@163.com
 */
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
