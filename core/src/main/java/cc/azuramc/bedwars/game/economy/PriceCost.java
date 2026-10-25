package cc.azuramc.bedwars.game.economy;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author an5w1r@163.com
 */
@Value
@Accessors(fluent = true)
public class PriceCost {
    /** Resource id of generators.yml */
    String currency;
    int amount;
    int xp;

    public static PriceCost teamUpgrade(int diamonds) {
        return new PriceCost("diamond", diamonds, diamonds * 100);
    }
}
