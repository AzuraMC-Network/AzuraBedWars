package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
public class UtilityShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public UtilityShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.TNT()).setLores("§e点击查看！").getItem(), "§a实用道具", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.FIREBALL()).getItem(), "火球", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 50, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.TNT()).getItem(), "TNT", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 4, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.ENDER_PEARL()).getItem(), "末影珍珠", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 4, 400)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WATER_BUCKET()).getItem(), "水桶", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.COMPASS()).setDisplayName("§e指南针").getItem(), "指南针", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BLAZE_ROD()).setDisplayName("§e援救平台").setLores(" ", "§f在空中使用该平台", "§f将在脚下生成一块临时史莱姆方块平台救生!").getItem(), "救援平台", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 80)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.GUNPOWDER()).setDisplayName("§e回城卷轴").setLores(" ", "§f将在6秒后传送到出生点.", "§f注意:移动将会取消传送!").getItem(), "回城卷轴", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 10, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BED()).setDisplayName("§e回春床").setLores(" ", "§f开局10分钟内可恢复一次床").getItem(), "回春床", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 80)));
    }

}
