package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;
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
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.TNT.get()).setLores("§e点击查看！").getItem(), "§a实用道具", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.FIRE_CHARGE.get()).getItem(), "火球", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 50, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.TNT.get()).getItem(), "TNT", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 4, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.ENDER_PEARL.get()).getItem(), "末影珍珠", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 4, 400)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WATER_BUCKET.get()).getItem(), "水桶", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
//        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.COMPASS.get()).setDisplayName("§e指南针").getItem(), "指南针", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.BLAZE_ROD.get()).setDisplayName("§e救援平台").setLores(" ", "§f在空中使用该平台", "§f将在脚下生成一块临时史莱姆方块平台救生!").getItem(), "救援平台", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 80)));
//        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GUNPOWDER.get()).setDisplayName("§e回城卷轴").setLores(" ", "§f将在6秒后传送到出生点.", "§f注意:移动将会取消传送!").getItem(), "回城卷轴", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 10, 50)));
//        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.RED_BED.get()).setDisplayName("§e回春床").setLores(" ", "§f开局10分钟内可恢复一次床").getItem(), "回春床", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 80)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GOLDEN_APPLE.get()).getItem(), "金苹果", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 3, 15)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.EGG.get()).getItem(), "搭桥蛋", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.SNOWBALL.get()).getItem(), "蠹虫", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 40, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WOLF_SPAWN_EGG.get()).getItem(), "梦幻守护者", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 128, 128)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.CHEST.get()).getItem(), "速建防御塔", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 64, 64)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.SPONGE.get()).getItem(),"海绵",ColorType.NONE,new PriceCost(XMaterial.GOLD_INGOT.get(),2,20))); // 没写逻辑
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.MILK_BUCKET.get()).getItem(),"魔法牛奶",ColorType.NONE,new PriceCost(XMaterial.GOLD_INGOT.get(),4,40))); // 没写逻辑

    }
}
