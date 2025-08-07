package cc.azuramc.bedwars.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
 */
@Data
@AllArgsConstructor
public class ShopItemType {
    private ItemStack itemStack;
    private String displayName;
    private ColorType colorType;
    private PriceCost priceCost;

    public ShopItemType(ItemStack itemStack, String displayName) {
        this.itemStack = itemStack;
        this.displayName = displayName;
    }
}
