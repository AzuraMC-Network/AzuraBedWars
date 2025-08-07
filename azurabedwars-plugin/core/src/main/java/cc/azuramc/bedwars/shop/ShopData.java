package cc.azuramc.bedwars.shop;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
public interface ShopData {
    /**
     * 获取标签页相关信息
     *
     * @return 返回商店最上栏标签页信息
     */
    ShopItemType getMainShopItem();

    /**
     * 返回标签页内商品信息
     *
     * @return 返回对应标签内的商品内容
     */
    List<ShopItemType> getShopItems();
}
