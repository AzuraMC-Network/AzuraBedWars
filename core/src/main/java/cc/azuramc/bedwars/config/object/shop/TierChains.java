package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Comment({"Tier chains: things like pickaxes, axes and armor that only go up", "Keyed by chain id, shop items point at one with tier: id"})
public class TierChains extends LinkedHashMap<String, TierChains.Chain> {

    @Data
    public static class Chain {
        @Comment("Name")
        private String name = "";

        @Comment("SEQUENTIAL buys one tier at a time, ANY can skip to a higher tier")
        private BuyOrder buyOrder = BuyOrder.SEQUENTIAL;

        @Comment({"On death", "KEEP keeps it, DOWNGRADE drops one tier but never below the first, RESET goes back to start"})
        private OnDeath onDeath = OnDeath.KEEP;

        @Comment("Takes the previous tier when a new one is bought")
        private boolean replacePrevious = true;

        @Comment("Tier owned on spawn, 0 is none")
        @Range(min = 0)
        private int start;

        @Comment("INVENTORY puts it in the inventory, ARMOR switches leggings and boots to the material of this tier")
        private Slot slot = Slot.INVENTORY;

        @Comment("Levels, the first entry is level 1")
        private List<Tier> tiers = new ArrayList<>();
    }

    @Data
    public static class Tier {
        @Comment("Name")
        private String name = "";

        @Comment("Item given by an INVENTORY chain")
        @Nullable
        private ItemSpec item;

        @Comment("Material prefix of an ARMOR chain, such as IRON")
        @Nullable
        @Raw
        private String materialPrefix;

        @Comment("Price, the tier is not sold when left out")
        @Nullable
        private Price price;

        @Comment("Price in levels for experience mode, converted from the xp-value of the resource when left out")
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
