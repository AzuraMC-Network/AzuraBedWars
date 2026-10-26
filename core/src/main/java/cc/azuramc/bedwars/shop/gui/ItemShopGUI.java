package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.ShopMenuConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import cc.azuramc.bedwars.shop.ItemSpecs;
import cc.azuramc.bedwars.shop.QuickBuy;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopCategory;
import cc.azuramc.bedwars.shop.ShopItem;
import cc.azuramc.bedwars.shop.ShopPurchase;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ItemShopGUI extends CustomGUI {

    /** Inventory titles longer than this throw before 1.14 */
    private static final int LEGACY_TITLE_LIMIT = 32;

    private final GamePlayer gamePlayer;
    private final int categoryIndex;
    private final GameManager gameManager;
    private final ShopCatalog catalog;

    /**
     * @param categoryIndex 0 for quick buy, then the categories in menu order
     */
    public ItemShopGUI(GamePlayer gamePlayer, int categoryIndex, GameManager gameManager) {
        this(gamePlayer, categoryIndex, gameManager, ShopCatalog.get());
    }

    private ItemShopGUI(GamePlayer gamePlayer, int categoryIndex, GameManager gameManager, ShopCatalog catalog) {
        super(gamePlayer, title(catalog, categoryIndex), 54);
        this.gamePlayer = gamePlayer;
        this.categoryIndex = categoryIndex;
        this.gameManager = gameManager;
        this.catalog = catalog;
        ShopMenuConfig menu = catalog.getMenu();

        List<ShopCategory> categories = catalog.getCategories();
        ItemStack selected = navigationPane(menu.getNavigation().getSelected());
        ItemStack unselected = navigationPane(menu.getNavigation().getUnselected());
        for (int i = 0; i <= categories.size() && i < 9; i++) {
            int target = i;
            ItemStack icon = i == 0 ? catalog.getQuickBuyIcon() : categories.get(i - 1).getIcon();
            setItem(i, icon.clone(), new GUIAction(0, () -> {
                if (target != categoryIndex) {
                    new ItemShopGUI(gamePlayer, target, gameManager).open();
                }
            }, false));
        }
        for (int i = 0; i < 9; i++) {
            setItem(i + 9, (i == categoryIndex ? selected : unselected).clone(), new GUIAction(0, () -> {
            }, false));
        }

        if (categoryIndex == 0) {
            setupQuickBuy();
        } else {
            for (Map.Entry<Integer, ShopItem> entry : categories.get(categoryIndex - 1).getItems().entrySet()) {
                ShopItem item = entry.getValue();
                setShopItem(entry.getKey(), item, null, () -> new QuickBuyEditGUI(gamePlayer, gameManager, item.getRef()).open());
            }
        }
    }

    private static String title(ShopCatalog catalog, int categoryIndex) {
        String name = categoryIndex == 0 ? catalog.getMenu().getQuickBuy().getName() : catalog.getCategories().get(categoryIndex - 1).getName();
        String title = catalog.getMenu().getTitle().replace("{category}", ChatColor.stripColor(name));
        return VersionUtil.isLessThan(1, 14) && title.length() > LEGACY_TITLE_LIMIT ? title.substring(0, LEGACY_TITLE_LIMIT) : title;
    }

    private static ItemStack navigationPane(ItemSpec spec) {
        ItemStack pane = ItemSpecs.build(spec, null);
        return pane != null ? pane : XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
    }

    private void setupQuickBuy() {
        PlayerData playerData = gamePlayer.getPlayerData();
        Map<Integer, String> layout = QuickBuy.load(playerData);
        ShopMenuConfig.QuickBuy menu = catalog.getMenu().getQuickBuy();
        List<Integer> slots = catalog.getQuickBuySlots();

        for (int i = 0; i < slots.size(); i++) {
            int index = i;
            ShopItem custom = QuickBuy.customItem(catalog, layout, index);
            if (custom != null) {
                setShopItem(slots.get(index), custom, menu.getRemoveHint(), () -> {
                    QuickBuy.remove(playerData, index);
                    new ItemShopGUI(gamePlayer, categoryIndex, gameManager).open();
                });
                continue;
            }

            ShopItem fallback = index < catalog.getQuickBuyDefaults().size() ? catalog.getQuickBuyDefaults().get(index) : null;
            if (fallback != null) {
                setShopItem(slots.get(index), fallback, menu.getAddHint(),
                        () -> new QuickBuyEditGUI(gamePlayer, gameManager, fallback.getRef()).open());
                continue;
            }

            setItem(slots.get(index), new ItemBuilder()
                            .setItemStack(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                            .setDisplayName("§c空闲的槽位")
                            .setLores("§7这是一个快捷购买槽位!§bShift+左键", "§7将任意物品放到这里~")
                            .getItem(),
                    new NewGUIAction(0, event -> {
                        if (event.isShiftClick()) {
                            gamePlayer.sendMessage("§c这是个空的槽位!请使用Shift+左键添加物品到这里~");
                        }
                    }, false));
        }
    }

    private void setShopItem(int displaySlot, ShopItem item, @Nullable String hint, Runnable onShiftClick) {
        ShopMenuConfig menu = catalog.getMenu();
        String price = gameManager.getEconomy().formatPrice(ShopPurchase.priceFor(gamePlayer, item));
        List<String> lore = new ArrayList<>();
        for (String line : menu.getLore()) {
            switch (line.trim()) {
                case "{tiers}":
                    lore.addAll(tierLore(item));
                    break;
                case "{hint}":
                    if (hint != null && !hint.isEmpty()) {
                        lore.add(hint);
                    }
                    break;
                case "{status}":
                    String status = status(menu.getStatus(), ShopPurchase.state(gamePlayer, item));
                    if (!status.isEmpty()) {
                        lore.add(status);
                    }
                    break;
                default:
                    lore.add(line.replace("{name}", item.getName()).replace("{price}", price));
                    break;
            }
        }

        setItem(displaySlot,
                new ItemBuilder().setItemStack(ShopPurchase.displayIcon(gamePlayer, item))
                        .setDisplayName(menu.getItemName().replace("{name}", item.getName()))
                        .setLores(lore)
                        .getItem(),
                new NewGUIAction(0, event -> {
                    if (event.isShiftClick()) {
                        onShiftClick.run();
                        return;
                    }
                    if (ShopPurchase.buy(gamePlayer, item) && (item.getChain() != null || item.getSpec().getLimit() != null)) {
                        new ItemShopGUI(gamePlayer, categoryIndex, gameManager).open();
                    }
                }, false));
    }

    private List<String> tierLore(ShopItem item) {
        List<String> lines = new ArrayList<>();
        TierChains.Chain chain = item.getChain();
        if (chain == null || item.getSpec().getLevel() > 0) {
            return lines;
        }
        ShopMenuConfig.TierLore format = catalog.getMenu().getTierLore();
        int current = gamePlayer.getTierLevel(item.getSpec().getTier());
        lines.add(format.getHeader().replace("{current}", String.valueOf(current)).replace("{max}", String.valueOf(chain.getTiers().size())));
        for (int level = 1; level <= chain.getTiers().size(); level++) {
            TierChains.Tier tier = chain.getTiers().get(level - 1);
            String template = level <= current ? format.getOwned() : level == current + 1 ? format.getNext() : format.getLocked();
            String price = gameManager.getEconomy().formatPrice(ShopPurchase.cost(tier.getPrice(), tier.getXp()));
            lines.add(template.replace("{name}", tier.getName()).replace("{price}", price));
        }
        String onDeath = chain.getOnDeath() == TierChains.OnDeath.DOWNGRADE ? format.getDowngrade()
                : chain.getOnDeath() == TierChains.OnDeath.RESET ? format.getReset() : format.getKeep();
        if (!onDeath.isEmpty()) {
            lines.add(onDeath);
        }
        return lines;
    }

    private static String status(ShopMenuConfig.Status status, ShopPurchase.State state) {
        switch (state) {
            case TOO_POOR:
                return status.getTooPoor();
            case OWNED:
                return status.getOwned();
            case MAXED:
                return status.getMaxed();
            case LOCKED:
                return status.getLocked();
            default:
                return status.getCanBuy();
        }
    }
}
