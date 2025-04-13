package cc.azuramc.bedwars.shop.model;

import java.util.List;

public interface ShopData {
    ShopItemType getMainShopItem();

    List<ShopItemType> getShopItems();
}
