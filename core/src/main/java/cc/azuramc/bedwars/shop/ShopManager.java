package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.shop.page.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class ShopManager {
    @Getter
    private static final List<ShopData> SHOPS = new ArrayList<>();

    public static void init(GameManager gameManager) {
        registerShop(new DefaultShopPage());
        registerShop(new BlockShopPage());
        registerShop(new SwordShopPage());
        registerShop(new ArmorShopPage());
        registerShop(new ToolShopPage());
        registerShop(new BowShopPage());
        registerShop(new PotionShopPage());
        registerShop(new UtilityShopPage());
        registerShop(new ConvertShopPage());
    }

    public static void registerShop(ShopData shopData) {
        SHOPS.add(shopData);
    }

    public static ShopData getShop(String name) {
        for (ShopData shop : SHOPS) {
            if (shop.getClass().getName().equals(name)) {
                return shop;
            }
        }

        return null;
    }
}
