package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.shop.data.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ItemShopManager {
    @Getter
    private static final List<ShopData> shops = new ArrayList<>();

    public static void init(GameManager gameManager) {
        registerShop(new DefaultShop());
        registerShop(new BlockShop());
        registerShop(new SwordShop());
        registerShop(new ArmorShop());
        registerShop(new ToolShop());
        registerShop(new BowShop());
        registerShop(new PotionShop());
        registerShop(new FoodShop());
        registerShop(new UtilityShop());
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
