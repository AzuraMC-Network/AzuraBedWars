package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public final class QuickBuy {

    public static final int[] SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final Gson GSON = new Gson();
    private static final Type LAYOUT_TYPE = new TypeToken<Map<Integer, String>>() {
    }.getType();

    private QuickBuy() {
    }

    /**
     * Stored layout format, slot index to {@code "<category id>#<1 based item index>"}
     */
    public static Map<Integer, String> load(PlayerData playerData) {
        String json = playerData.getShopDataJson();
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            Map<Integer, String> layout = GSON.fromJson(json, LAYOUT_TYPE);
            return layout != null ? layout : new HashMap<>();
        } catch (JsonParseException e) {
            LoggerUtil.warn("Invalid quick buy layout of " + playerData.getName() + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void set(PlayerData playerData, int slot, String key) {
        Map<Integer, String> layout = load(playerData);
        layout.put(slot, key);
        playerData.setShopDataJson(GSON.toJson(layout));
    }

    public static void remove(PlayerData playerData, int slot) {
        Map<Integer, String> layout = load(playerData);
        layout.remove(slot);
        playerData.setShopDataJson(GSON.toJson(layout));
    }

    public static String keyOf(ShopCategory category, int index) {
        return category.getId() + "#" + (index + 1);
    }

    @Nullable
    public static ShopItem customItem(Map<Integer, String> layout, int slot) {
        String key = layout.get(slot);
        return key != null ? resolve(key) : null;
    }

    @Nullable
    public static ShopItem resolve(String key) {
        String[] parts = key.split("#");
        if (parts.length != 2) {
            return null;
        }
        try {
            return ShopCatalog.find(parts[0], Integer.parseInt(parts[1]) - 1);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static ShopItem defaultItem(int slot) {
        List<ShopItem> defaults = ShopCatalog.getQuickBuy().getItems();
        return slot < defaults.size() ? defaults.get(slot) : null;
    }
}
