package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.shop.page.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class ShopManager {
    @Getter
    @NotNull
    private static final List<ShopData> SHOPS = new ArrayList<>();

    public static void init(@NotNull GameManager gameManager) {
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

    public static void registerShop(@NotNull ShopData shopData) {
        SHOPS.add(shopData);
    }

    @Nullable
    public static ShopData getShop(@NotNull String name) {
        for (ShopData shop : SHOPS) {
            if (shop.getClass().getName().equals(name)) {
                return shop;
            }
        }

        return null;
    }
}
