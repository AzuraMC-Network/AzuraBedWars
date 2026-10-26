package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author an5w1r@163.com
 */
@Data
@NoArgsConstructor
public class Limit {

    @Comment("Most purchases per game")
    @Range(min = 1)
    private int times = 1;

    public Limit(int times) {
        this.times = times;
    }

    public static Limit fromString(String text) {
        if ("once".equalsIgnoreCase(text)) {
            return new Limit(1);
        }
        try {
            int times = Integer.parseInt(text);
            if (times >= 1) {
                return new Limit(times);
            }
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Write once or a number of purchases of at least 1");
    }
}
