package cc.azuramc.bedwars.shop;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Value
public class ShopCategory {
    /** Stored in quick buy layouts, named after the old page classes */
    String id;
    ItemStack icon;
    String name;
    List<ShopItem> items;
}
