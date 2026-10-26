package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public final class QuickBuy {

    private static final Gson GSON = new Gson();
    private static final Type LAYOUT_TYPE = new TypeToken<Map<Integer, String>>() {
    }.getType();

    /**
     * Items of the shop pages before shop/ existed, in their old order. Layouts stored then use
     * {@code "<page class>#<1 based index>"}
     */
    private static final Map<String, List<String>> LEGACY_PAGES = new HashMap<>();

    static {
        LEGACY_PAGES.put("DefaultShopPage", Arrays.asList("blocks.wool", "melee.stone_sword", "armor.chainmail",
                "utility.golden_apple", "ranged.bow", "potions.speed", "utility.tnt", "blocks.planks", "melee.iron_sword",
                "armor.iron", "utility.fireball", "ranged.arrows", "potions.jump", "utility.water"));
        LEGACY_PAGES.put("BlockShopPage", Arrays.asList("blocks.wool", "blocks.speed_wool", "blocks.clay", "blocks.glass",
                "blocks.endstone", "blocks.ladder", "blocks.planks", "blocks.slime", "blocks.cobweb", "blocks.obsidian"));
        LEGACY_PAGES.put("SwordShopPage", Arrays.asList("melee.stone_sword", "melee.iron_sword", "melee.diamond_sword",
                "melee.knockback_stick"));
        LEGACY_PAGES.put("ArmorShopPage", Arrays.asList("armor.chainmail", "armor.iron", "armor.diamond"));
        LEGACY_PAGES.put("ToolShopPage", Arrays.asList("tools.pickaxe", "tools.axe", "tools.shears"));
        LEGACY_PAGES.put("BowShopPage", Arrays.asList("ranged.arrows", "ranged.bow", "ranged.power_bow", "ranged.punch_bow"));
        LEGACY_PAGES.put("PotionShopPage", Arrays.asList("potions.speed", "potions.jump", "potions.invisibility"));
        LEGACY_PAGES.put("UtilityShopPage", Arrays.asList("utility.fireball", "utility.tnt", "utility.ender_pearl",
                "utility.water", "utility.rescue_platform", "utility.golden_apple", "utility.bridge_egg",
                "utility.silverfish", "utility.dream_defender", "utility.popup_tower", "utility.magic_milk"));
        LEGACY_PAGES.put("ConvertShopPage", Arrays.asList("convert.iron", "convert.gold", "convert.diamond", "convert.emerald"));
    }

    private QuickBuy() {
    }

    /**
     * Stored layout, index of the quick buy slot to {@code <category>.<id>}. Old keys are converted and written back
     */
    public static Map<Integer, String> load(PlayerData playerData) {
        String json = playerData.getShopDataJson();
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        Map<Integer, String> layout;
        try {
            layout = GSON.fromJson(json, LAYOUT_TYPE);
        } catch (JsonParseException e) {
            LoggerUtil.warn("Invalid quick buy layout of " + playerData.getName() + ": " + e.getMessage());
            return new HashMap<>();
        }
        if (layout == null) {
            return new HashMap<>();
        }
        boolean converted = false;
        for (Map.Entry<Integer, String> entry : layout.entrySet()) {
            String ref = convertLegacy(entry.getValue());
            if (ref != null) {
                entry.setValue(ref);
                converted = true;
            }
        }
        if (converted) {
            playerData.setShopDataJson(GSON.toJson(layout));
        }
        return layout;
    }

    public static void set(PlayerData playerData, int index, String ref) {
        Map<Integer, String> layout = load(playerData);
        layout.put(index, ref);
        playerData.setShopDataJson(GSON.toJson(layout));
    }

    public static void remove(PlayerData playerData, int index) {
        Map<Integer, String> layout = load(playerData);
        layout.remove(index);
        playerData.setShopDataJson(GSON.toJson(layout));
    }

    @Nullable
    public static ShopItem customItem(ShopCatalog catalog, Map<Integer, String> layout, int index) {
        String ref = layout.get(index);
        return ref != null ? catalog.find(ref) : null;
    }

    @Nullable
    static String convertLegacy(String key) {
        String[] parts = key.split("#");
        List<String> page = parts.length == 2 ? LEGACY_PAGES.get(parts[0]) : null;
        if (page == null) {
            return null;
        }
        try {
            int index = Integer.parseInt(parts[1]) - 1;
            return index >= 0 && index < page.size() ? page.get(index) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
