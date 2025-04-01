package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class UtilityShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public UtilityShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.TNT()).setLores("§e点击查看！").getItem(), "§a实用道具", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.FIREBALL()).getItem(), "火球", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 50, 50)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.TNT()).getItem(), "TNT", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 4, 40)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.ENDER_PEARL()).getItem(), "末影珍珠", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 4, 400)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WATER_BUCKET()).getItem(), "水桶", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.COMPASS()).setDisplayName("§e指南针").getItem(), "指南针", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BLAZE_ROD()).setDisplayName("§e援救平台").setLores(" ", "§f在空中使用该平台", "§f将在脚下生成一块临时史莱姆方块平台救生!").getItem(), "救援平台", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 80)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.GUNPOWDER()).setDisplayName("§e回城卷轴").setLores(" ", "§f将在6秒后传送到出生点.", "§f注意:移动将会取消传送!").getItem(), "回城卷轴", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 10, 50)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BED()).setDisplayName("§e回春床").setLores(" ", "§f开局10分钟内可恢复一次床").getItem(), "回春床", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 80)));
    }

}
