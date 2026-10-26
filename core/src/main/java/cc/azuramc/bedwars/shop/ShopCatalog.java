package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.game.item.special.SpecialItems;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The shop built from shop/ and generators.yml, rebuilt when one of those documents is replaced
 *
 * @author an5w1r@163.com
 */
public final class ShopCatalog {

    private static final List<Integer> DEFAULT_QUICK_BUY_SLOTS = Arrays.asList(
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43);

    @Nullable
    private static ShopCatalog cached;

    private final List<Object> sources;
    @Getter
    private final ShopMenuConfig menu;
    @Getter
    private final TierChains tiers;
    /** Categories after quick buy, in menu order */
    @Getter
    private final List<ShopCategory> categories = new ArrayList<>();
    private final Map<String, ShopItem> items = new LinkedHashMap<>();
    @Getter
    private final List<Integer> quickBuySlots = new ArrayList<>();
    @Getter
    private final List<ShopItem> quickBuyDefaults = new ArrayList<>();
    @Getter
    private final ItemStack quickBuyIcon;

    private ShopCatalog(List<Object> sources, ShopMenuConfig menu, TierChains tiers,
                        Map<String, ShopCategoryConfig> configs, EconomyType economy) {
        this.sources = sources;
        this.menu = menu;
        this.tiers = tiers;

        quickBuySlots.addAll(slots(menu.getQuickBuy().getLayout(), ShopValidator.QUICK_BUY_SLOT));
        if (quickBuySlots.isEmpty()) {
            quickBuySlots.addAll(DEFAULT_QUICK_BUY_SLOTS);
        }
        quickBuyIcon = icon(menu.getQuickBuy().getIcon(), menu.getQuickBuy().getName());

        for (Map.Entry<String, ShopCategoryConfig> category : configs.entrySet()) {
            for (Map.Entry<String, ShopItemSpec> entry : category.getValue().getItems().entrySet()) {
                ShopItemSpec spec = entry.getValue();
                String ref = category.getKey() + "." + ShopValidator.itemId(entry.getKey(), spec);
                ShopItem item = spec.getRef() == null && visible(spec.getEconomy(), category.getValue().getEconomy(), economy)
                        ? build(ref, spec, tiers) : null;
                if (item != null) {
                    items.put(ref, item);
                }
            }
        }

        for (String id : menu.getCategories()) {
            ShopCategoryConfig config = configs.get(id);
            if (config == null || config.getEconomy() != null && config.getEconomy() != economy) {
                continue;
            }
            Map<Integer, ShopItem> placed = new LinkedHashMap<>();
            int next = 0;
            for (Map.Entry<String, ShopItemSpec> entry : config.getItems().entrySet()) {
                ShopItemSpec spec = entry.getValue();
                String ref = id + "." + ShopValidator.itemId(entry.getKey(), spec);
                ShopItem item = items.get(spec.getRef() != null ? spec.getRef() : ref);
                if (item == null) {
                    continue;
                }
                items.put(ref, item);
                if (config.getLayout().isEmpty()) {
                    if (next < quickBuySlots.size()) {
                        placed.put(quickBuySlots.get(next++), item);
                    }
                } else if (entry.getKey().length() == 1) {
                    for (Integer slot : slots(config.getLayout(), entry.getKey().charAt(0))) {
                        placed.put(slot, item);
                    }
                }
            }
            categories.add(new ShopCategory(id, icon(config.getIcon(), config.getName()), config.getName(), Collections.unmodifiableMap(placed)));
        }

        for (String ref : menu.getQuickBuy().getDefaults()) {
            ShopItem item = items.get(ref);
            if (item != null) {
                quickBuyDefaults.add(item);
            }
        }
    }

    public static ShopCatalog get() {
        AzuraBedWars plugin = AzuraBedWars.getInstance();
        ConfigService service = plugin.getConfigService();
        ShopMenuConfig menu = service.get(ShopMenuConfig.class);
        TierChains tiers = service.get(TierChains.class);
        Map<String, ShopCategoryConfig> configs = new LinkedHashMap<>();
        for (Map.Entry<String, ShopCategoryConfig> entry : service.all(ShopCategoryConfig.class).entrySet()) {
            configs.put(ConfigService.name(entry.getKey()), entry.getValue());
        }
        EconomyType economy = plugin.getGameManager().getEconomyType();

        List<Object> sources = new ArrayList<>(Arrays.asList(menu, tiers, plugin.getGeneratorConfig(), economy));
        sources.addAll(configs.values());
        ShopCatalog catalog = cached;
        if (catalog == null || !sameObjects(catalog.sources, sources)) {
            catalog = new ShopCatalog(sources, menu, tiers, configs, economy);
            cached = catalog;
        }
        return catalog;
    }

    /**
     * @param ref {@code <category>.<id>}
     */
    @Nullable
    public ShopItem find(String ref) {
        return items.get(ref);
    }

    /**
     * @param level 1 for the first tier
     */
    @Nullable
    public static ItemStack tierItem(TierChains.Chain chain, int level) {
        TierChains.Tier tier = chain.getTiers().get(level - 1);
        return tier.getItem() != null ? ItemSpecs.build(tier.getItem(), tier.getName()) : null;
    }

    @Nullable
    private static ShopItem build(String ref, ShopItemSpec spec, TierChains tiers) {
        TierChains.Chain chain = null;
        if (spec.getTier() != null) {
            chain = tiers.get(spec.getTier());
            if (chain == null) {
                return null;
            }
        }
        ItemStack item = null;
        if (spec.getItem() != null) {
            item = ItemSpecs.build(spec.getItem(), spec.getName());
            if (item == null) {
                return null;
            }
            if (spec.getSpecial() != null) {
                item = SpecialItems.tag(item, spec.getSpecial());
            }
        } else if (chain == null) {
            return null;
        }
        return new ShopItem(ref, spec.getName(), spec, item, chain);
    }

    private static boolean visible(@Nullable EconomyType item, @Nullable EconomyType category, EconomyType economy) {
        EconomyType only = item != null ? item : category;
        return only == null || only == economy;
    }

    private ItemStack icon(ItemSpec spec, String name) {
        ItemStack base = ItemSpecs.build(spec, null);
        return new ItemBuilder().setItemStack(base != null ? base : new ItemStack(Material.STONE))
                .setDisplayName(name)
                .setLores(menu.getNavigation().getCategoryLore())
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .getItem();
    }

    public static List<Integer> slots(List<String> layout, char key) {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < layout.size() && row < SpecChecks.ROWS; row++) {
            String line = layout.get(row);
            for (int column = 0; column < line.length() && column < SpecChecks.COLUMNS; column++) {
                if (line.charAt(column) == key) {
                    slots.add(row * SpecChecks.COLUMNS + column);
                }
            }
        }
        return slots;
    }

    private static boolean sameObjects(List<Object> a, List<Object> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }
}
