package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.shop.type.ItemType;

import java.util.List;

public interface ShopData {
    ItemType getMainShopItem();

    List<ItemType> getShopItems();
}
