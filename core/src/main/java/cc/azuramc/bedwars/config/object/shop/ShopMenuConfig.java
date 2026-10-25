package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Raw;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("商店外框")
public class ShopMenuConfig {

    @Comment({"商店标题", "变量: {category} 当前分类名"})
    private String title = "&8道具商店 - {category}";

    @Comment({"分类顺序，最多 8 个，第 1 格固定是快速购买", "写 shop/categories 里的文件名"})
    @Raw
    private List<String> categories = new ArrayList<>();

    private Navigation navigation = new Navigation();

    @Comment({"商品说明文字", "变量: {name} 商品名, {price} 价格", "单独一行的 {tiers} {hint} {status} 会换成升级链、快速购买提示和购买状态，没有内容时这一行去掉"})
    private List<String> lore = new ArrayList<>();

    @Comment("商品图标名称，变量: {name}")
    private String itemName = "&c{name}";

    private Status status = new Status();

    private QuickBuy quickBuy = new QuickBuy();

    private TierLore tierLore = new TierLore();

    @Data
    public static class Navigation {
        @Comment("当前分类下方的玻璃板")
        private ItemSpec selected = new ItemSpec("LIME_STAINED_GLASS_PANE");

        @Comment("其他格子的玻璃板")
        private ItemSpec unselected = new ItemSpec("GRAY_STAINED_GLASS_PANE");

        @Comment("分类图标的说明文字")
        private List<String> categoryLore = new ArrayList<>(Arrays.asList("&e点击查看！"));
    }

    @Data
    public static class Status {
        private String canBuy = "&e点击购买";
        private String tooPoor = "&c资源不足";
        private String owned = "&a已拥有";
        private String maxed = "&a已满级";
        private String locked = "&c需要先购买上一级";
    }

    @Data
    public static class QuickBuy {
        private ItemSpec icon = new ItemSpec("NETHER_STAR");

        private String name = "&b快速购买";

        @Comment({"6 行 9 列，Q 是玩家可以自定义的格子", "没写 layout 的分类也按这些格子排列商品"})
        private List<String> layout = new ArrayList<>();

        @Comment("默认商品，依次放进 Q 格子，写 分类.商品id")
        @Raw
        private List<String> defaults = new ArrayList<>();

        private String addHint = "&7Shift+左键添加到快捷购买";

        private String removeHint = "&7Shift+左键从快捷购买中移除";
    }

    @Data
    public static class TierLore {
        @Comment("变量: {current} 当前等级, {max} 最高等级")
        private String header = "&7等级 {current}/{max}";

        @Comment("变量: {name} 这一级的名称, {price} 价格")
        private String owned = "&a✔ {name}";

        private String next = "&e➜ {name} &7- {price}";

        private String locked = "&8✖ {name} - {price}";

        private String keep = "";

        private String downgrade = "&7死亡后降一级";

        private String reset = "&7死亡后回到初始";
    }
}
