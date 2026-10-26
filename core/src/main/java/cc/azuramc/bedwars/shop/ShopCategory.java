package cc.azuramc.bedwars.shop;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author an5w1r@163.com
 */
@Value
public class ShopCategory {
    String id;
    ItemStack icon;
    String name;
    /** Inventory slot to item */
    Map<Integer, ShopItem> items;
}
