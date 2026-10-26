package cc.azuramc.bedwars.config.object.shop;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.game.economy.EconomyType;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
public class ShopItemSpec {

    @Comment({"Item id, the key when left out", "Quick buy and ref point at items as category.id"})
    @Nullable
    @Raw
    private String id;

    @Comment("Item name")
    private String name = "";

    @Comment("Item given, a lone material can be written as item: WHITE_WOOL")
    @Nullable
    private ItemSpec item;

    @Comment("Price, amount and resource id, such as 4 iron")
    @Nullable
    private Price price;

    @Comment("Price in levels for experience mode, converted from the xp-value of the resource when left out")
    @Nullable
    @Range(min = 0)
    private Integer xp;

    @Comment("Tier chain id from tiers.yml, the icon and price follow the tier of the player")
    @Nullable
    @Raw
    private String tier;

    @Comment({"Target tier of the chain, 1 is the first", "The next tier when left out"})
    @Range(min = 0)
    private int level;

    @Comment("Takes the wooden sword and applies the team sharpness")
    private boolean sword;

    @Comment({"Built-in special item", "fireball, tnt, popup_tower, rescue_platform, warp_powder, bridge_egg, silverfish, dream_defender, magic_milk, invisibility, speed_wool"})
    @Nullable
    @Raw
    private String special;

    @Comment({"Commands run by the console after a purchase", "Placeholders: {player} player name, {team} team name"})
    @Raw
    private List<String> commands = new ArrayList<>();

    @Comment("once for a single purchase, a number for most purchases per game")
    @Nullable
    private Limit limit;

    @Comment("Given again on respawn and reconnect")
    private boolean keepOnRespawn;

    @Comment("Only shown in this economy mode")
    @Nullable
    private EconomyType economy;

    @Comment("Reuses an item of another category, such as blocks.wool, no other field is needed")
    @Nullable
    @Raw
    private String ref;

    @Comment("Purchase requirements")
    @Nullable
    private Requires requires;

    @Data
    public static class Requires {
        @Comment("Required permission")
        @Nullable
        @Raw
        private String permission;
    }
}
