package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author an5w1r@163.com
 */
@Data
@NoArgsConstructor
public class Price {

    @Comment("Amount")
    @Range(min = 0)
    private int amount;

    @Comment("Resource id from generators.yml")
    @Raw
    private String currency = "iron";

    public Price(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Price fromString(String text) {
        String[] parts = text.trim().split("\\s+");
        if (parts.length == 2) {
            try {
                int amount = Integer.parseInt(parts[0]);
                if (amount >= 0) {
                    return new Price(amount, parts[1]);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("Write a price as an amount and a resource id, such as 4 iron");
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
