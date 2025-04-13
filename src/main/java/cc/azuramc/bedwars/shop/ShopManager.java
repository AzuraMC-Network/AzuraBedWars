package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.page.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {
    @Getter
    private static final List<ShopData> shops = new ArrayList<>();

    public static void init(GameManager gameManager) {
        registerShop(new DefaultShopPage());
        registerShop(new BlockShopPage());
        registerShop(new SwordShopPage());
        registerShop(new ArmorShopPage());
        registerShop(new ToolShopPage());
        registerShop(new BowShopPage());
        registerShop(new PotionShopPage());
        registerShop(new FoodShopPage());
        registerShop(new UtilityShopPage());
    }

    public static void registerShop(ShopData shopData) {
        shops.add(shopData);
    }

    public static ShopData getShop(String name) {
        for (ShopData shop : shops) {
            if (shop.getClass().getName().equals(name)) {
                return shop;
            }
        }

        return null;
    }
}
