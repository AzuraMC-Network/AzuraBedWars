package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Raw;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("Shop frame")
public class ShopMenuConfig {

    @Comment({"Shop title", "Placeholders: {category} name of the current category"})
    private String title = "&8Item Shop - {category}";

    @Comment({"Category order, up to 8, the first slot is always quick buy", "File names in shop/categories"})
    @Raw
    private List<String> categories = new ArrayList<>();

    private Navigation navigation = new Navigation();

    @Comment({"Item lore", "Placeholders: {name} item name, {price} price", "A line holding only {tiers} {hint} or {status} becomes the tier list, quick buy hint or purchase status, and is dropped when empty"})
    private List<String> lore = new ArrayList<>();

    @Comment("Item icon name, placeholders: {name}")
    private String itemName = "&c{name}";

    private Status status = new Status();

    private QuickBuy quickBuy = new QuickBuy();

    private TierLore tierLore = new TierLore();

    @Data
    public static class Navigation {
        @Comment("Glass pane under the current category")
        private ItemSpec selected = new ItemSpec("LIME_STAINED_GLASS_PANE");

        @Comment("Glass pane in the other slots")
        private ItemSpec unselected = new ItemSpec("GRAY_STAINED_GLASS_PANE");

        @Comment("Lore of the category icons")
        private List<String> categoryLore = new ArrayList<>(Arrays.asList("&eClick to view!"));
    }

    @Data
    public static class Status {
        private String canBuy = "&eClick to purchase!";
        private String tooPoor = "&cYou don't have enough resources!";
        private String owned = "&aUNLOCKED";
        private String maxed = "&aMAXED";
        private String locked = "&cBuy the previous tier first";
    }

    @Data
    public static class QuickBuy {
        private ItemSpec icon = new ItemSpec("NETHER_STAR");

        private String name = "&bQuick Buy";

        @Comment({"6 rows of 9, Q marks the slots players can customize", "Categories without a layout place their items in the same slots"})
        private List<String> layout = new ArrayList<>();

        @Comment("Default items placed in the Q slots in order, written as category.item id")
        @Raw
        private List<String> defaults = new ArrayList<>();

        private String addHint = "&7Shift Click to add to Quick Buy";

        private String removeHint = "&7Shift Click to remove from Quick Buy";
    }

    @Data
    public static class TierLore {
        @Comment("Placeholders: {current} current tier, {max} highest tier")
        private String header = "&7Tier {current}/{max}";

        @Comment("Placeholders: {name} name of this tier, {price} price")
        private String owned = "&a✔ {name}";

        private String next = "&e➜ {name} &7- {price}";

        private String locked = "&8✖ {name} - {price}";

        private String keep = "";

        private String downgrade = "&7Goes down a tier on death";

        private String reset = "&7Resets on death";
    }
}
