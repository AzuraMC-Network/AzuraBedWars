package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@NoArgsConstructor
public class ItemSpec {

    @Comment("Material, a modern XSeries name")
    @Raw
    private String material = "STONE";

    @Range(min = 1, max = 64)
    private int amount = 1;

    @Comment("Display name, the shop item name when left out")
    @Nullable
    private String name;

    private List<String> lore = new ArrayList<>();

    @Comment("Enchantment names and levels")
    @Raw
    private LinkedHashMap<String, Integer> enchants = new LinkedHashMap<>();

    @Comment("Hidden tooltips, such as HIDE_ATTRIBUTES, HIDE_ENCHANTS")
    @Raw
    private List<String> flags = new ArrayList<>();

    private boolean unbreakable;

    @Comment("Dye wool, glass, terracotta and leather in the team color")
    private boolean teamColor;

    @Nullable
    private Potion potion;

    @Comment("Mob of a spawn egg")
    @Nullable
    @Raw
    private String spawnEgg;

    public ItemSpec(String material) {
        this.material = material;
    }

    public static ItemSpec fromString(String material) {
        return new ItemSpec(material);
    }

    @Data
    public static class Potion {
        @Comment("Potion effect name")
        @Raw
        private String effect = "SPEED";

        @Comment("Seconds")
        @Range(min = 0)
        private double seconds = 30;

        @Comment("Effect level, 1 is I")
        @Range(min = 1, max = 256)
        private int level = 1;
    }
}
