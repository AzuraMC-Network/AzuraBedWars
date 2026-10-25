package cc.azuramc.bedwars.shop;

import com.cryptomorin.xseries.XMaterial;
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

    public static PriceCost teamUpgrade(int diamonds) {
        return new PriceCost(XMaterial.DIAMOND.get(), diamonds, diamonds * 100);
    }
}
