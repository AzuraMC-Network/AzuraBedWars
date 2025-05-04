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
public class FoodShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public FoodShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.COOKED_PORKCHOP.get()).setLores("§e点击查看！").getItem(), "§a食物", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GOLDEN_APPLE.get()).getItem(), "金苹果", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 3, 15)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.COOKED_BEEF.get()).setAmount(2).getItem(), "牛排", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.CARROT.get()).setAmount(2).getItem(), "胡萝卜", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 1, 4)));
    }

}
