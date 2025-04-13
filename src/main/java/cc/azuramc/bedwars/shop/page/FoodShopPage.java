package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.model.ColorType;
import cc.azuramc.bedwars.shop.model.ShopItemType;
import cc.azuramc.bedwars.shop.model.PriceCost;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class FoodShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public FoodShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.COOKED_PORKCHOP()).setLores("§e点击查看！").getItem(), "§a食物", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.GOLDEN_APPLE()).getItem(), "金苹果", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 3, 15)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.COOKED_BEEF()).setAmount(2).getItem(), "牛排", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.CARROT()).setAmount(2).getItem(), "胡萝卜", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 1, 4)));
    }

}
