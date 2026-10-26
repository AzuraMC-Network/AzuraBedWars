package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.Price;
import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.devtools.Args;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.devtools.ToolError;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.shop.ItemSpecs;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopCategory;
import cc.azuramc.bedwars.shop.ShopItem;
import cc.azuramc.bedwars.shop.ShopPurchase;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.upgrade.UpgradeValidator;
import cc.azuramc.bedwars.upgrade.gui.TeamUpgradeGUI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public final class ShopTools {

    private static final String QUICK_BUY = "quick_buy";
    private static final int ROWS = 6;
    private static final int COLUMNS = 9;

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    public ShopTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    public List<McpTool> tools() {
        return Arrays.asList(
                McpTool.read("shop_render", "Draw a shop page as the 6 by 9 grid players see. Every symbol in the grid is explained in cells"
                                + " with its item, price and tier. quick_buy shows the default quick buy. Call it after changing shop/ files")
                        .string("category", "Category id from the categories of shop/menu.yml, or quick_buy", true)
                        .choice("economy", "Economy to show prices in, default the one of settings.yml", false, "ITEM", "EXPERIENCE")
                        .param("tiers", tiersSchema("Owned level per tier chain, default the start level of each chain"), false)
                        .handle(args -> devtools.sync(() -> renderShop(args))),
                McpTool.read("upgrade_render", "Draw the team upgrades and traps page as the 6 by 9 grid players see, for a team that owns nothing yet")
                        .choice("economy", "Economy to show prices in, default the one of settings.yml", false, "ITEM", "EXPERIENCE")
                        .handle(args -> devtools.sync(() -> renderUpgrades(args))),
                McpTool.write("shop_open", "Open the shop or the team upgrades for an online player, so the admin sees it in game. Needs game mode")
                        .string("player", "Online player name", true)
                        .choice("page", "Which menu, default shop", false, "shop", "upgrades")
                        .string("category", "Shop category id or quick_buy, default quick_buy", false)
                        .param("tiers", tiersSchema("Set these owned tier levels on the player first, to see later tiers"), false)
                        .handle(args -> devtools.sync(() -> open(args)))
        );
    }

    private static JsonObject tiersSchema(String description) {
        JsonObject schema = McpTool.Builder.type("object", description + ", such as {\"pickaxe\": 2}");
        schema.add("additionalProperties", McpTool.Builder.type("integer", "Owned level, 0 is none"));
        return schema;
    }

    private JsonElement renderShop(Args args) {
        EconomyType economy = economy(args);
        ShopCatalog catalog = ShopCatalog.get(economy);
        Map<String, Integer> tiers = tiers(args, catalog.getTiers());
        String category = args.string("category");
        List<ShopCategory> categories = catalog.getCategories();
        int index = -1;
        if (QUICK_BUY.equals(category)) {
            index = 0;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(category)) {
                index = i + 1;
            }
        }
        if (index < 0) {
            List<String> ids = new ArrayList<>();
            ids.add(QUICK_BUY);
            categories.forEach(c -> ids.add(c.getId()));
            throw new ToolError("No category " + category + " is shown in the " + economy + " economy, shown: " + String.join(", ", ids)
                    + ". A category is left out when shop/menu.yml does not list it or its economy differs");
        }

        Grid grid = new Grid();
        for (int i = 0; i <= categories.size() && i < COLUMNS; i++) {
            JsonObject tab = new JsonObject();
            tab.addProperty("kind", i == index ? "tab, selected" : "tab");
            tab.addProperty("name", strip(i == 0 ? catalog.getMenu().getQuickBuy().getName() : categories.get(i - 1).getName()));
            tab.addProperty("category", i == 0 ? QUICK_BUY : categories.get(i - 1).getId());
            tab.addProperty("material", material(i == 0 ? catalog.getQuickBuyIcon() : categories.get(i - 1).getIcon()));
            grid.put(i, "tab:" + i, tab);
        }
        for (int i = 0; i < COLUMNS; i++) {
            grid.put(i + COLUMNS, i == index ? "pane:selected" : "pane", decor(i == index ? "selected tab marker" : "divider",
                    i == index ? catalog.getMenu().getNavigation().getSelected() : catalog.getMenu().getNavigation().getUnselected()));
        }

        EconomyMode mode = economy.getMode();
        if (index == 0) {
            List<Integer> slots = catalog.getQuickBuySlots();
            for (int i = 0; i < slots.size(); i++) {
                ShopItem item = i < catalog.getQuickBuyDefaults().size() ? catalog.getQuickBuyDefaults().get(i) : null;
                if (item != null) {
                    grid.put(slots.get(i), item.getRef(), item(item, tiers, mode));
                } else {
                    JsonObject empty = new JsonObject();
                    empty.addProperty("kind", "empty quick buy slot");
                    grid.put(slots.get(i), "quick-buy-empty", empty);
                }
            }
        } else {
            for (Map.Entry<Integer, ShopItem> entry : categories.get(index - 1).getItems().entrySet()) {
                grid.put(entry.getKey(), entry.getValue().getRef(), item(entry.getValue(), tiers, mode));
            }
        }
        String name = index == 0 ? catalog.getMenu().getQuickBuy().getName() : categories.get(index - 1).getName();
        JsonObject result = grid.json(strip(catalog.getMenu().getTitle().replace("{category}", strip(name))));
        result.addProperty("economy", economy.name());
        return result;
    }

    private static JsonObject item(ShopItem item, Map<String, Integer> tiers, EconomyMode economy) {
        ShopItemSpec spec = item.getSpec();
        TierChains.Chain chain = item.getChain();
        JsonObject json = new JsonObject();
        json.addProperty("kind", "item");
        json.addProperty("ref", item.getRef());
        json.addProperty("name", strip(item.getName()));
        ItemStack icon = item.getItem();
        if (chain == null) {
            json.addProperty("price", strip(economy.formatPrice(ShopPurchase.cost(spec.getPrice(), spec.getXp()))));
        } else {
            int owned = tiers.get(spec.getTier());
            int level = spec.getLevel() > 0 ? spec.getLevel() : Math.min(owned + 1, chain.getTiers().size());
            TierChains.Tier tier = chain.getTiers().get(level - 1);
            if (icon == null) {
                icon = ShopCatalog.tierItem(chain, level);
            }
            json.addProperty("price", strip(economy.formatPrice(ShopPurchase.cost(tier.getPrice(), tier.getXp()))));
            json.addProperty("tier", spec.getTier() + " level " + level + " of " + chain.getTiers().size() + ", owned " + owned
                    + (spec.getLevel() == 0 && owned >= chain.getTiers().size() ? ", maxed" : ""));
        }
        if (icon != null) {
            json.addProperty("material", material(icon));
            if (icon.getAmount() > 1) {
                json.addProperty("amount", icon.getAmount());
            }
        }
        if (spec.getLimit() != null) {
            json.addProperty("limit", spec.getLimit().getTimes());
        }
        if (spec.getSpecial() != null) {
            json.addProperty("special", spec.getSpecial());
        }
        if (spec.getCustom() != null) {
            json.addProperty("custom", spec.getCustom());
        }
        if (spec.getAction() != null) {
            json.addProperty("action", spec.getAction());
        }
        return json;
    }

    private JsonElement renderUpgrades(Args args) {
        EconomyMode economy = economy(args).getMode();
        UpgradeConfig config = plugin.getUpgradeConfig();
        Grid grid = new Grid();
        for (Map.Entry<String, ItemSpec> entry : config.getDecor().entrySet()) {
            if (entry.getKey().length() == 1) {
                for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                    grid.put(slot, "decor:" + entry.getKey(), decor("decor", entry.getValue()));
                }
            }
        }
        for (Map.Entry<String, UpgradeConfig.Upgrade> entry : config.getUpgrades().entrySet()) {
            UpgradeConfig.Upgrade upgrade = entry.getValue();
            if (entry.getKey().length() != 1) {
                continue;
            }
            JsonObject json = new JsonObject();
            json.addProperty("kind", "upgrade");
            json.addProperty("id", UpgradeValidator.upgradeId(entry.getKey(), upgrade));
            json.addProperty("name", strip(upgrade.getName()));
            json.addProperty("material", material(upgrade.getIcon()));
            JsonArray levels = new JsonArray();
            for (UpgradeConfig.Level level : upgrade.getLevels()) {
                levels.add(new JsonPrimitive(strip(level.getName()) + ": " + strip(economy.formatPrice(ShopPurchase.cost(level.getPrice(), level.getXp())))));
            }
            json.add("levels", levels);
            for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                grid.put(slot, "upgrade:" + entry.getKey(), json);
            }
        }
        UpgradeConfig.Traps traps = config.getTraps();
        List<Price> prices = traps.getPrices().isEmpty() ? new UpgradeConfig.Traps().getPrices() : traps.getPrices();
        String firstTrap = strip(economy.formatPrice(ShopPurchase.cost(prices.get(0), traps.getXp().isEmpty() ? null : traps.getXp().get(0))));
        for (Map.Entry<String, UpgradeConfig.Trap> entry : traps.getTypes().entrySet()) {
            if (entry.getKey().length() != 1) {
                continue;
            }
            JsonObject json = new JsonObject();
            json.addProperty("kind", "trap");
            json.addProperty("id", UpgradeValidator.trapId(entry.getKey(), entry.getValue()));
            json.addProperty("name", strip(entry.getValue().getName()));
            json.addProperty("material", material(entry.getValue().getIcon()));
            json.addProperty("price", firstTrap + " for the first trap, later ones follow traps in upgrades.yml");
            for (int slot : ShopCatalog.slots(config.getLayout(), entry.getKey().charAt(0))) {
                grid.put(slot, "trap:" + entry.getKey(), json);
            }
        }
        List<Integer> queue = ShopCatalog.slots(config.getLayout(), UpgradeValidator.QUEUE_SLOT);
        for (int i = 0; i < queue.size() && i < traps.getQueueSize(); i++) {
            JsonObject json = new JsonObject();
            json.addProperty("kind", "trap queue, empty");
            json.addProperty("material", material(config.getText().getQueueEmpty()));
            grid.put(queue.get(i), "queue", json);
        }
        return grid.json(strip(config.getTitle()));
    }

    private JsonElement open(Args args) {
        GameManager gameManager = plugin.getGameManager();
        if (gameManager == null) {
            throw new ToolError("Menus open in game mode only, the server runs in editor mode. shop_render and upgrade_render work in both");
        }
        Player player = Bukkit.getPlayerExact(args.string("player"));
        GamePlayer gamePlayer = player != null ? GamePlayer.get(player) : null;
        if (gamePlayer == null) {
            throw new ToolError("Player " + args.string("player") + " is not online");
        }
        if (args.has("tiers")) {
            Map<String, Integer> tiers = tiers(args, ShopCatalog.get().getTiers());
            for (Map.Entry<String, JsonElement> entry : args.object("tiers").entrySet()) {
                gamePlayer.getTierLevels().put(entry.getKey(), tiers.get(entry.getKey()));
            }
        }
        if ("upgrades".equals(args.optString("page"))) {
            if (gamePlayer.getGameTeam() == null) {
                throw new ToolError(player.getName() + " has no team yet, team upgrades open once the game started");
            }
            new TeamUpgradeGUI(gamePlayer, gameManager).open();
            return new JsonPrimitive("Opened the team upgrades for " + player.getName());
        }
        String category = args.has("category") ? args.string("category") : QUICK_BUY;
        int index = QUICK_BUY.equals(category) ? 0 : -1;
        List<ShopCategory> categories = ShopCatalog.get().getCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(category)) {
                index = i + 1;
            }
        }
        if (index < 0) {
            throw new ToolError("No category " + category + " is shown, see shop_render");
        }
        new ItemShopGUI(gamePlayer, index, gameManager).open();
        return new JsonPrimitive("Opened " + category + " for " + player.getName());
    }

    private EconomyType economy(Args args) {
        if (args.has("economy")) {
            return args.choice("economy", EconomyType.class);
        }
        return plugin.getGameManager() != null ? plugin.getGameManager().getEconomyType() : plugin.getSettingsConfig().getEconomyMode();
    }

    private static Map<String, Integer> tiers(Args args, TierChains chains) {
        Map<String, Integer> tiers = new HashMap<>();
        for (Map.Entry<String, TierChains.Chain> entry : chains.entrySet()) {
            tiers.put(entry.getKey(), entry.getValue().getStart());
        }
        if (!args.has("tiers")) {
            return tiers;
        }
        JsonObject given = args.object("tiers");
        Args levels = new Args(given);
        for (Map.Entry<String, JsonElement> entry : given.entrySet()) {
            TierChains.Chain chain = chains.get(entry.getKey());
            if (chain == null) {
                throw new ToolError("No tier chain " + entry.getKey() + ", chains: " + String.join(", ", chains.keySet()));
            }
            int level = levels.integer(entry.getKey());
            if (level < 0 || level > chain.getTiers().size()) {
                throw new ToolError(entry.getKey() + " has levels 0 to " + chain.getTiers().size());
            }
            tiers.put(entry.getKey(), level);
        }
        return tiers;
    }

    private static JsonObject decor(String kind, ItemSpec spec) {
        JsonObject json = new JsonObject();
        json.addProperty("kind", kind);
        json.addProperty("material", material(spec));
        return json;
    }

    private static String material(ItemSpec spec) {
        ItemStack item = ItemSpecs.build(spec, null);
        return item != null ? material(item) : "unknown material " + spec.getMaterial();
    }

    /**
     * Legacy servers tell colors apart by data value, such as STAINED_GLASS_PANE:7
     */
    @SuppressWarnings("deprecation")
    private static String material(ItemStack item) {
        return item.getType().name() + (item.getDurability() != 0 ? ":" + item.getDurability() : "");
    }

    private static String strip(@Nullable String text) {
        return text == null ? "" : ChatColor.stripColor(text);
    }

    /**
     * Inventory slots as one symbol each, the same symbol for every slot showing the same thing
     */
    private static final class Grid {
        private static final String SYMBOLS = "ABCDEFGHIJKLMNOPRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        private final String[] cells = new String[ROWS * COLUMNS];
        private final Map<String, String> symbols = new HashMap<>();
        private final Map<String, JsonObject> legend = new LinkedHashMap<>();

        Grid() {
            Arrays.fill(cells, ".");
        }

        void put(int slot, String key, JsonObject info) {
            if (slot < 0 || slot >= cells.length) {
                return;
            }
            String symbol = symbols.get(key);
            if (symbol == null) {
                symbol = symbols.size() < SYMBOLS.length() ? String.valueOf(SYMBOLS.charAt(symbols.size())) : "?";
                symbols.put(key, symbol);
                JsonObject entry = new JsonObject();
                for (Map.Entry<String, JsonElement> field : info.entrySet()) {
                    entry.add(field.getKey(), field.getValue());
                }
                entry.add("slots", new JsonArray());
                legend.put(symbol, entry);
            }
            cells[slot] = symbol;
            legend.get(symbol).getAsJsonArray("slots").add(new JsonPrimitive(slot));
        }

        JsonObject json(String title) {
            JsonObject json = new JsonObject();
            json.addProperty("title", title);
            JsonArray rows = new JsonArray();
            for (int row = 0; row < ROWS; row++) {
                rows.add(new JsonPrimitive(String.join(" ", Arrays.copyOfRange(cells, row * COLUMNS, (row + 1) * COLUMNS))));
            }
            json.add("rows", rows);
            json.addProperty("slots", "Slot numbers count from 0 at the top left, row by row, 9 per row. A dot is an empty slot");
            JsonObject cellsJson = new JsonObject();
            for (Map.Entry<String, JsonObject> entry : legend.entrySet()) {
                cellsJson.add(entry.getKey(), entry.getValue());
            }
            json.add("cells", cellsJson);
            return json;
        }
    }
}
