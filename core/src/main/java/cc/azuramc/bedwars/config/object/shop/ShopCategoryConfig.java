package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.game.economy.EconomyType;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"商店分类", "文件名就是分类 id，在 shop/menu.yml 的 categories 里排序"})
public class ShopCategoryConfig {

    @Comment("分类名")
    private String name = "";

    @Comment("分类图标")
    private ItemSpec icon = new ItemSpec("STONE");

    @Comment("只在这个经济模式出现")
    @Nullable
    private EconomyType economy;

    @Comment({"6 行 9 列的字符画，每个字符对应 items 里同名的键，空格是空位", "前两行是导航栏，必须留空", "不写时按顺序填进快速购买的格子"})
    private List<String> layout = new ArrayList<>();

    @Comment("商品")
    private LinkedHashMap<String, ShopItemSpec> items = new LinkedHashMap<>();
}
