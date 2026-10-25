package cc.azuramc.bedwars.shop;

import lombok.Value;
import lombok.experimental.Accessors;
import org.bukkit.Material;

/**
 * @author an5w1r@163.com
 */
@Value
@Accessors(fluent = true)
public class PriceCost {
    Material material;
    int amount;
    int xp;
}
