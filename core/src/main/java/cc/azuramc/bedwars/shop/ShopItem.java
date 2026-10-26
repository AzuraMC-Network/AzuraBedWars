package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import lombok.Value;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author an5w1r@163.com
 */
@Value
public class ShopItem {
    /** {@code <category>.<id>}, stored in quick buy layouts */
    String ref;
    String name;
    ShopItemSpec spec;
    /** Null for tier items that take their icon from the chain */
    @Nullable
    ItemStack item;
    @Nullable
    TierChains.Chain chain;
}
