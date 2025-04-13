package cc.azuramc.bedwars.gui.base.component;

import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
public class GUIItem {
    private int size;
    private ItemStack itemStack;
    private GUIAction guiAction;
    private NewGUIAction newGUIAction;
}
