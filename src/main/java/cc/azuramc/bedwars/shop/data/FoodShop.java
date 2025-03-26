package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class FoodShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public FoodShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.COOKED_PORKCHOP()).setLores("§e点击查看！").getItem(), "§a食物", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_APPLE()).getItem(), "金苹果", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 3, 15)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.COOKED_BEEF()).setAmount(2).getItem(), "牛排", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.CARROT()).setAmount(2).getItem(), "胡萝卜", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 1, 4)));
    }

}
