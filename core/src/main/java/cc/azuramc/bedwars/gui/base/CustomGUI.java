package cc.azuramc.bedwars.gui.base;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.component.GUIItem;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
public class CustomGUI {
    public List<GUIItem> items;
    private final GamePlayer gamePlayer;
    private final Player player;
    private final String title;
    private final int size;

    public CustomGUI(GamePlayer gamePlayer, String title, int size) {
        this.gamePlayer = gamePlayer;
        this.player = gamePlayer.getPlayer();
        this.title = title;
        this.size = size;

        this.items = new ArrayList<>();
    }

    public void setItem(int size, ItemStack itemStack, GUIAction guiAction) {
        items.add(new GUIItem(size, itemStack, guiAction, null));
    }

    public void setItem(int size, ItemStack itemStack, NewGUIAction guiAction) {
        items.add(new GUIItem(size, itemStack, null, guiAction));
    }

    public void open() {
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (GUIItem guiItem : items) {
            inventory.setItem(guiItem.getSize(), guiItem.getItemStack());
        }

        if (GUIData.getCURRENT_GUI().containsKey(player)) {
            GUIData.getLAST_GUI().put(player, GUIData.getCURRENT_GUI().get(player));
        }
        GUIData.getCURRENT_GUI().put(player, this);

        player.openInventory(inventory);
    }

    public void replace() {
        CustomGUI customGUI = GUIData.getLAST_REPLACE_GUI().getOrDefault(player, GUIData.getCURRENT_GUI().getOrDefault(player, null));
        if(customGUI == null) {
            return;
        }
        GUIData.getLAST_REPLACE_GUI().put(player, customGUI);

        for(GUIItem guiItem : customGUI.items){
            boolean s = true;
            for(GUIItem guiItem1 : items){
                if (guiItem.getSize() == guiItem1.getSize()) {
                    s = false;
                    break;
                }
            }
            if(s) {
                items.add(guiItem);
            }
        }

        open();
    }
}
