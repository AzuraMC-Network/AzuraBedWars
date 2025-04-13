package cc.azuramc.bedwars.shop;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public class PriceCost {
    private final Material material;
    private final int amount;
    private final int xp;

    public PriceCost(Material material, int amount, int xp) {
        this.material = material;
        this.amount = amount;
        this.xp = xp;
    }

}
