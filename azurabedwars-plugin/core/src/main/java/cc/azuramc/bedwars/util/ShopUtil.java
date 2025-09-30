package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.ShopManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ShopUtil {

    /**
     * 商店展示槽位
     */
    public static final Integer[] SHOP_SLOTS = new Integer[]{
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    /**
     * JSON处理器
     */
    private static final Gson GSON = new Gson();

    /**
     * 商店数据类型定义
     */
    private static final Type SHOP_DATA_TYPE = new TypeToken<Map<Integer, String>>() {
    }.getType();

    /**
     * 从数据库加载JSON格式的快捷商店配置
     * 解析JSON为Map<Integer, String>
     *
     * @param playerData 玩家数据
     * @return 商店配置Map，键为槽位索引，值为物品数据字符串
     */
    public static Map<Integer, String> loadShopDataFromJson(PlayerData playerData) {
        try {
            String shopDataJson = playerData.getShopDataJson();
            if (shopDataJson != null && !shopDataJson.trim().isEmpty()) {
                Map<Integer, String> shopData = GSON.fromJson(shopDataJson, SHOP_DATA_TYPE);
                return shopData != null ? shopData : new HashMap<>();
            }
        } catch (Exception e) {
            // JSON解析失败，返回空Map
            e.printStackTrace();
        }

        // 返回默认空配置
        return new HashMap<>();
    }

    /**
     * 将Map<Integer, String>保存为JSON格式到数据库
     *
     * @param playerData  玩家数据
     * @param shopDataMap 商店数据Map
     */
    public static void saveShopDataToJson(PlayerData playerData, Map<Integer, String> shopDataMap) {
        try {
            String jsonData = GSON.toJson(shopDataMap);
            playerData.setShopDataJson(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找物品类型
     *
     * @param itemInfo 物品信息数组，格式为 [商店类名, 物品索引]
     * @return 找到的商店物品类型，未找到则返回null
     */
    public static ShopItemType findItemType(String[] itemInfo) {
        if (itemInfo == null || itemInfo.length != 2) {
            return null;
        }

        for (ShopData shopData : ShopManager.getSHOPS()) {
            if (shopData.getClass().getSimpleName().equals(itemInfo[0])) {
                try {
                    int itemIndex = Integer.parseInt(itemInfo[1]) - 1;
                    if (itemIndex >= 0 && itemIndex < shopData.getShopItems().size()) {
                        return shopData.getShopItems().get(itemIndex);
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 检查快捷商店数据是否为空
     *
     * @param shopDataMap 商店数据Map
     * @return 如果Map为空或所有槽位都是null/"AIR"，返回true
     */
    public static boolean isShopDataEmpty(Map<Integer, String> shopDataMap) {
        if (shopDataMap == null || shopDataMap.isEmpty()) {
            return true;
        }

        // 检查是否所有槽位都是空的
        for (String itemData : shopDataMap.values()) {
            if (itemData != null && !"AIR".equals(itemData)) {
                return false;
            }
        }

        return true;
    }
}
