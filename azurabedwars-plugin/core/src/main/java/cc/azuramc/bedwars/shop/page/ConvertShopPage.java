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
public class ConvertShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public ConvertShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.EXPERIENCE_BOTTLE.get()).setLores("§e点击查看！").getItem(), "§a经验转换", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.IRON_INGOT.get()).setAmount(1).getItem(), "铁锭", ColorType.NONE, new PriceCost(XMaterial.OAK_BUTTON.get(), 1, 1)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GOLD_INGOT.get()).setAmount(1).getItem(), "金锭", ColorType.NONE, new PriceCost(XMaterial.OAK_BUTTON.get(), 1, 5)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.DIAMOND.get()).setAmount(1).getItem(), "钻石", ColorType.NONE, new PriceCost(XMaterial.OAK_BUTTON.get(), 1, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.EMERALD.get()).setAmount(1).getItem(), "绿宝石", ColorType.NONE, new PriceCost(XMaterial.OAK_BUTTON.get(), 1, 100)));
    }

}
