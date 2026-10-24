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

    /**
     * 通过简单类名获取商店
     *
     * @param simpleName 商店类的简单名称（如 "BlockShopPage"）
     * @return 商店数据，未找到返回 null
     */
    @Nullable
    public static ShopData getShopBySimpleName(@NotNull String simpleName) {
        for (ShopData shop : SHOPS) {
            if (shop.getClass().getSimpleName().equals(simpleName)) {
                return shop;
            }
        }
        return null;
    }

    /**
     * 通过索引获取商店
     *
     * @param index 商店索引
     * @return 商店数据，索引无效返回 null
     */
    @Nullable
    public static ShopData getShop(int index) {
        if (index < 0 || index >= SHOPS.size()) {
            return null;
        }
        return SHOPS.get(index);
    }
}
