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

    @Comment("材质，使用 XSeries 新版名称")
    @Raw
    private String material = "STONE";

    @Range(min = 1, max = 64)
    private int amount = 1;

    @Comment("显示名称，不写时商品用商品名")
    @Nullable
    private String name;

    private List<String> lore = new ArrayList<>();

    @Comment("附魔名称和等级")
    @Raw
    private LinkedHashMap<String, Integer> enchants = new LinkedHashMap<>();

    @Comment("隐藏的提示，例如 HIDE_ATTRIBUTES, HIDE_ENCHANTS")
    @Raw
    private List<String> flags = new ArrayList<>();

    private boolean unbreakable;

    @Comment("羊毛、玻璃、陶瓦、皮革染成队伍颜色")
    private boolean teamColor;

    @Nullable
    private Potion potion;

    @Comment("刷怪蛋生成的生物")
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
        @Comment("药水效果名称")
        @Raw
        private String effect = "SPEED";

        @Comment("持续秒数")
        @Range(min = 0)
        private double seconds = 30;

        @Comment("效果等级，1 是 I 级")
        @Range(min = 1, max = 256)
        private int level = 1;
    }
}
