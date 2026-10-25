package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.ConfigValidator;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.game.item.special.SpecialItems;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Checks the references between generators.yml and the shop files
 *
 * @author an5w1r@163.com
 */
public final class ShopValidator implements ConfigValidator {

    public static final int NAVIGATION_ROWS = 2;
    public static final int MAX_CATEGORIES = 8;
    public static final char QUICK_BUY_SLOT = 'Q';

    private final SpecChecks checks;

    public ShopValidator(SpecChecks.Names names) {
        this.checks = new SpecChecks(names);
    }

    @Override
    public void validate(View view, Sink sink) {
        GeneratorConfig generators = view.get(GeneratorConfig.class);
        Set<String> currencies = generators.getResources().keySet();
        checkGenerators(view.idOf(GeneratorConfig.class), generators, sink);

        String tiersId = view.idOf(TierChains.class);
        TierChains tiers = view.get(TierChains.class);
        for (Map.Entry<String, TierChains.Chain> entry : tiers.entrySet()) {
            checkChain(tiersId, entry.getKey(), entry.getValue(), currencies, sink);
        }

        Map<String, String> documents = new LinkedHashMap<>();
        Map<String, ShopCategoryConfig> categories = new LinkedHashMap<>();
        for (Map.Entry<String, ShopCategoryConfig> entry : view.all(ShopCategoryConfig.class).entrySet()) {
            String name = ConfigService.name(entry.getKey());
            documents.put(name, entry.getKey());
            categories.put(name, entry.getValue());
        }
        Map<String, ShopItemSpec> items = new HashMap<>();
        for (Map.Entry<String, ShopCategoryConfig> category : categories.entrySet()) {
            for (Map.Entry<String, ShopItemSpec> item : category.getValue().getItems().entrySet()) {
                items.put(category.getKey() + "." + itemId(item.getKey(), item.getValue()), item.getValue());
            }
        }
        for (Map.Entry<String, ShopCategoryConfig> category : categories.entrySet()) {
            checkCategory(documents.get(category.getKey()), category.getValue(), tiers, currencies, items, sink);
        }

        checkMenu(view.idOf(ShopMenuConfig.class), view.get(ShopMenuConfig.class), categories.keySet(), items.keySet(), sink);
    }

    public static String itemId(String key, ShopItemSpec spec) {
        return spec.getId() != null ? spec.getId() : key;
    }

    private void checkGenerators(String document, GeneratorConfig generators, Sink sink) {
        for (Map.Entry<String, GeneratorConfig.Resource> entry : generators.getResources().entrySet()) {
            String path = "resources." + entry.getKey();
            GeneratorConfig.Resource resource = entry.getValue();
            checks.material(document, path + ".material", resource.getMaterial(), sink);
            if (resource.getBlock() != null) {
                checks.material(document, path + ".block", resource.getBlock(), sink);
            }
            if (resource.getLevels().isEmpty()) {
                sink.error(document, path + ".levels", "A resource needs at least one level");
            }
            if (resource.getAt() == GeneratorConfig.Spawn.PUBLIC && resource.getDrop() == null) {
                sink.error(document, path + ".drop", "A PUBLIC resource needs the drop point type of the map, DIAMOND or EMERALD");
            }
        }
    }

    private void checkChain(String document, String id, TierChains.Chain chain, Set<String> currencies, Sink sink) {
        if (chain.getTiers().isEmpty()) {
            sink.error(document, id + ".tiers", "A tier chain needs at least one tier");
        }
        if (chain.getStart() > chain.getTiers().size()) {
            sink.error(document, id + ".start", "start is higher than the " + chain.getTiers().size() + " tiers");
        }
        for (int i = 0; i < chain.getTiers().size(); i++) {
            String path = id + ".tiers[" + i + "]";
            TierChains.Tier tier = chain.getTiers().get(i);
            if (chain.getSlot() == TierChains.Slot.ARMOR) {
                if (tier.getMaterialPrefix() == null) {
                    sink.error(document, path + ".material-prefix", "An ARMOR tier needs a material prefix such as IRON");
                } else if (!checks.names().material(tier.getMaterialPrefix() + "_BOOTS")) {
                    sink.warn(document, path + ".material-prefix", "No " + tier.getMaterialPrefix() + "_BOOTS on this server, the tier gives nothing");
                }
            } else if (tier.getItem() == null) {
                sink.error(document, path + ".item", "An INVENTORY tier needs an item");
            } else {
                checks.item(document, path + ".item", tier.getItem(), sink);
            }
            SpecChecks.price(document, path + ".price", tier.getPrice(), currencies, sink);
        }
    }

    private void checkCategory(String document, ShopCategoryConfig category, TierChains tiers, Set<String> currencies,
                               Map<String, ShopItemSpec> items, Sink sink) {
        checks.item(document, "icon", category.getIcon(), sink);
        Set<String> ids = new HashSet<>();
        for (Map.Entry<String, ShopItemSpec> entry : category.getItems().entrySet()) {
            String path = "items." + entry.getKey();
            ShopItemSpec item = entry.getValue();
            if (!ids.add(itemId(entry.getKey(), item))) {
                sink.error(document, path + ".id", "Another item of this category already has the id " + itemId(entry.getKey(), item));
            }
            if (item.getRef() != null) {
                ShopItemSpec target = items.get(item.getRef());
                if (target == null) {
                    sink.error(document, path + ".ref", SpecChecks.unknown("item", item.getRef(), items.keySet()));
                } else if (target.getRef() != null) {
                    sink.error(document, path + ".ref", "The ref points at " + item.getRef() + ", which is a ref itself");
                }
                continue;
            }
            checkShopItem(document, path, item, category.getEconomy(), tiers, currencies, sink);
        }
        if (!category.getLayout().isEmpty()) {
            Set<String> placed = SpecChecks.layout(document, "layout", category.getLayout(), NAVIGATION_ROWS, sink);
            for (int row = 0; row < category.getLayout().size(); row++) {
                String line = category.getLayout().get(row);
                for (int column = 0; column < line.length(); column++) {
                    String key = String.valueOf(line.charAt(column));
                    if (line.charAt(column) != ' ' && !category.getItems().containsKey(key)) {
                        sink.error(document, "layout[" + row + "]", "No item has the key " + key);
                    }
                }
            }
            for (String key : category.getItems().keySet()) {
                if (!placed.contains(key)) {
                    sink.warn(document, "items." + key, "The layout has no " + key + ", the item is not shown");
                }
            }
        }
    }

    private void checkShopItem(String document, String path, ShopItemSpec item, @Nullable EconomyType categoryEconomy,
                               TierChains tiers, Set<String> currencies, Sink sink) {
        if (item.getItem() != null) {
            checks.item(document, path + ".item", item.getItem(), sink);
        }
        if (item.getTier() != null) {
            TierChains.Chain chain = tiers.get(item.getTier());
            if (chain == null) {
                sink.error(document, path + ".tier", SpecChecks.unknown("tier chain", item.getTier(), tiers.keySet()));
            } else if (item.getLevel() > chain.getTiers().size()) {
                sink.error(document, path + ".level", "The tier chain " + item.getTier() + " has only " + chain.getTiers().size() + " tiers");
            }
        } else if (item.getItem() == null) {
            sink.error(document, path + ".item", "An item needs item or tier");
        }
        EconomyType economy = item.getEconomy() != null ? item.getEconomy() : categoryEconomy;
        if (item.getPrice() == null && item.getTier() == null && economy != EconomyType.EXPERIENCE) {
            sink.error(document, path + ".price", "Missing price, write it as an amount and a resource id such as 4 iron");
        }
        SpecChecks.price(document, path + ".price", item.getPrice(), currencies, sink);
        if (item.getSpecial() != null && !SpecialItems.IDS.contains(item.getSpecial())) {
            sink.error(document, path + ".special", SpecChecks.unknown("special item", item.getSpecial(), SpecialItems.IDS));
        }
        if (item.getCustom() != null && item.getAction() != null) {
            sink.error(document, path + ".action", "An item has either custom or action, not both");
        } else if ((item.getCustom() != null || item.getAction() != null) && item.getTier() != null) {
            sink.error(document, path + ".tier", "A tier item cannot have custom or action");
        }
    }

    private void checkMenu(String document, ShopMenuConfig menu, Set<String> categories, Set<String> items, Sink sink) {
        if (menu.getCategories().size() > MAX_CATEGORIES) {
            sink.error(document, "categories", "At most " + MAX_CATEGORIES + " categories fit next to quick buy");
        }
        for (int i = 0; i < menu.getCategories().size(); i++) {
            if (!categories.contains(menu.getCategories().get(i))) {
                sink.error(document, "categories[" + i + "]", SpecChecks.unknown("category", menu.getCategories().get(i), categories));
            }
        }
        checks.item(document, "navigation.selected", menu.getNavigation().getSelected(), sink);
        checks.item(document, "navigation.unselected", menu.getNavigation().getUnselected(), sink);
        checks.item(document, "quick-buy.icon", menu.getQuickBuy().getIcon(), sink);
        SpecChecks.layout(document, "quick-buy.layout", menu.getQuickBuy().getLayout(), NAVIGATION_ROWS, sink);
        for (int i = 0; i < menu.getQuickBuy().getDefaults().size(); i++) {
            String ref = menu.getQuickBuy().getDefaults().get(i);
            if (!items.contains(ref)) {
                sink.error(document, "quick-buy.defaults[" + i + "]", SpecChecks.unknown("item", ref, items));
            }
        }
    }
}
