package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Comment({"升级链：镐、斧、盔甲这类有等级、只往上走的东西", "键是升级链 id，商品用 tier: id 引用"})
public class TierChains extends LinkedHashMap<String, TierChains.Chain> {

    @Data
    public static class Chain {
        @Comment("名称")
        private String name = "";

        @Comment("SEQUENTIAL 只能逐级买，ANY 可以直接买更高级")
        private BuyOrder buyOrder = BuyOrder.SEQUENTIAL;

        @Comment({"死亡后", "KEEP 保留, DOWNGRADE 降一级但不低于第一级, RESET 回到 start"})
        private OnDeath onDeath = OnDeath.KEEP;

        @Comment("买新的一级时拿走上一级")
        private boolean replacePrevious = true;

        @Comment("出生时拥有的等级，0 是没有")
        @Range(min = 0)
        private int start;

        @Comment("INVENTORY 放进背包，ARMOR 把护腿和靴子换成这一级的材质")
        private Slot slot = Slot.INVENTORY;

        @Comment("各级，第一项是 1 级")
        private List<Tier> tiers = new ArrayList<>();
    }

    @Data
    public static class Tier {
        @Comment("名称")
        private String name = "";

        @Comment("INVENTORY 升级链发放的物品")
        @Nullable
        private ItemSpec item;

        @Comment("ARMOR 升级链的材质前缀，例如 IRON")
        @Nullable
        @Raw
        private String materialPrefix;

        @Comment("价格，不写时这一级不出售")
        @Nullable
        private Price price;

        @Comment("经验模式的价格(级)，不写时按资源的 xp-value 换算")
        @Nullable
        @Range(min = 0)
        private Integer xp;
    }

    public enum BuyOrder {
        SEQUENTIAL,
        ANY
    }

    public enum OnDeath {
        KEEP,
        DOWNGRADE,
        RESET
    }

    public enum Slot {
        INVENTORY,
        ARMOR
    }
}
