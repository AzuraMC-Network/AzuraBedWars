package cc.azuramc.bedwars.shop.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
public class ItemType {
    private ItemStack itemStack;
    private String displayName;
    private ColorType colorType;
    private PriceCost priceCost;

    public ItemType(ItemStack itemStack, String displayName) {
        this.itemStack = itemStack;
        this.displayName = displayName;
    }
}
