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
@Comment({"Shop category", "The file name is the category id, ordered by categories in shop/menu.yml"})
public class ShopCategoryConfig {

    @Comment("Category name")
    private String name = "";

    @Comment("Category icon")
    private ItemSpec icon = new ItemSpec("STONE");

    @Comment("Only shown in this economy mode")
    @Nullable
    private EconomyType economy;

    @Comment({"6 rows of 9 characters, each one the key of an entry in items, a space is an empty slot", "The first two rows are the navigation bar and must stay empty", "Items fill the quick buy slots in order when left out"})
    private List<String> layout = new ArrayList<>();

    @Comment("Items")
    private LinkedHashMap<String, ShopItemSpec> items = new LinkedHashMap<>();
}
