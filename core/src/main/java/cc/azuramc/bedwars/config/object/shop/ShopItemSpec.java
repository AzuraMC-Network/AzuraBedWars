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

    @Comment({"商品 id，不写时用键名", "快速购买和 ref 用 分类.id 引用商品"})
    @Nullable
    @Raw
    private String id;

    @Comment("商品名")
    private String name = "";

    @Comment("发放的物品，只写材质时可以简写成 item: WHITE_WOOL")
    @Nullable
    private ItemSpec item;

    @Comment("价格，数量 资源id，例如 4 iron")
    @Nullable
    private Price price;

    @Comment("经验模式的价格(级)，不写时按资源的 xp-value 换算")
    @Nullable
    @Range(min = 0)
    private Integer xp;

    @Comment("tiers.yml 里的升级链 id，图标和价格随玩家当前等级变化")
    @Nullable
    @Raw
    private String tier;

    @Comment({"升级链的目标等级，1 是第一级", "不写时购买下一级"})
    @Range(min = 0)
    private int level;

    @Comment("拿走木剑并套上队伍锋利附魔")
    private boolean sword;

    @Comment({"内置特殊道具", "fireball, tnt, popup_tower, rescue_platform, warp_powder, bridge_egg, silverfish, dream_defender, magic_milk, invisibility, speed_wool"})
    @Nullable
    @Raw
    private String special;

    @Comment({"购买后以控制台身份执行的命令", "变量: {player} 玩家名, {team} 队伍名"})
    @Raw
    private List<String> commands = new ArrayList<>();

    @Comment("once 只能买一次，数字表示每局最多几次")
    @Nullable
    private Limit limit;

    @Comment("复活和重连时重新发放")
    private boolean keepOnRespawn;

    @Comment("只在这个经济模式出现")
    @Nullable
    private EconomyType economy;

    @Comment("复用另一个分类的商品，例如 blocks.wool，其余字段都不用写")
    @Nullable
    @Raw
    private String ref;

    @Comment("购买条件")
    @Nullable
    private Requires requires;

    @Data
    public static class Requires {
        @Comment("需要的权限")
        @Nullable
        @Raw
        private String permission;
    }
}
