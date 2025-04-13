package cc.azuramc.bedwars.shop;

import java.util.List;

public interface ShopData {
    ShopItemType getMainShopItem();

    List<ShopItemType> getShopItems();
}
