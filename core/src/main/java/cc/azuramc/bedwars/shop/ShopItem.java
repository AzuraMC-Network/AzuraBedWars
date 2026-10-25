package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import lombok.Value;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author an5w1r@163.com
 */
@Value
public class ShopItem {
    ItemStack icon;
    String name;
    ShopItemKind kind;
    /** Set only for {@link ShopItemKind#ARMOR} */
    @Nullable
    ArmorType armor;
    /** Null for pickaxes and axes priced by tier */
    @Nullable
    PriceCost price;
}
