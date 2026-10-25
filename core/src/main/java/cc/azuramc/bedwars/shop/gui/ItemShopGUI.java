package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import cc.azuramc.bedwars.shop.QuickBuy;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopCategory;
import cc.azuramc.bedwars.shop.ShopItem;
import cc.azuramc.bedwars.shop.ShopPurchase;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ItemShopGUI extends CustomGUI {

    private final GamePlayer gamePlayer;
    private final int categoryIndex;
    private final GameManager gameManager;

    public ItemShopGUI(GamePlayer gamePlayer, int categoryIndex, GameManager gameManager) {
        super(gamePlayer, "§8道具商店 - " + ChatColor.stripColor(ShopCatalog.getCategories().get(categoryIndex).getName()), 54);
        this.gamePlayer = gamePlayer;
        this.categoryIndex = categoryIndex;
        this.gameManager = gameManager;

        List<ShopCategory> categories = ShopCatalog.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            ShopCategory category = categories.get(i);
            int target = i;
            setItem(i, new ItemBuilder().setItemStack(category.getIcon().clone()).setDisplayName(category.getName()).getItem(),
                    new GUIAction(0, () -> {
                        if (target != categoryIndex) {
                            new ItemShopGUI(gamePlayer, target, gameManager).open();
                        }
                    }, false));
            setItem(i + 9, (i == categoryIndex ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.GRAY_STAINED_GLASS_PANE).parseItem(),
                    new GUIAction(0, () -> {
                    }, false));
        }
        for (int i = categories.size(); i < 9; i++) {
            setItem(i + 9, XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        if (categoryIndex == 0) {
            setupQuickBuy();
        } else {
            ShopCategory category = categories.get(categoryIndex);
            for (int i = 0; i < category.getItems().size(); i++) {
                String key = QuickBuy.keyOf(category, i);
                setShopItem(QuickBuy.SLOTS[i], category.getItems().get(i), null,
                        () -> new QuickBuyEditGUI(gamePlayer, gameManager, key).open());
            }
        }
    }

    private void setupQuickBuy() {
        PlayerData playerData = gamePlayer.getPlayerData();
        Map<Integer, String> layout = QuickBuy.load(playerData);
        ShopCategory quickBuy = ShopCatalog.getQuickBuy();

        for (int i = 0; i < QuickBuy.SLOTS.length; i++) {
            int slot = i;
            ShopItem custom = QuickBuy.customItem(layout, slot);
            if (custom != null) {
                setShopItem(QuickBuy.SLOTS[slot], custom, "§7Shift+左键从快捷购买中移除", () -> {
                    QuickBuy.remove(playerData, slot);
                    new ItemShopGUI(gamePlayer, categoryIndex, gameManager).open();
                });
                continue;
            }

            ShopItem fallback = QuickBuy.defaultItem(slot);
            if (fallback != null) {
                String key = QuickBuy.keyOf(quickBuy, slot);
                setShopItem(QuickBuy.SLOTS[slot], fallback, "§7Shift+左键添加到快捷购买",
                        () -> new QuickBuyEditGUI(gamePlayer, gameManager, key).open());
                continue;
            }

            setItem(QuickBuy.SLOTS[slot], new ItemBuilder()
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

    private void setShopItem(int displaySlot, ShopItem item, @Nullable String shiftHint, Runnable onShiftClick) {
        List<String> lore = new ArrayList<>();
        lore.add("§7物品:");
        lore.add("§8•" + item.getName());
        lore.add(" ");
        if (shiftHint != null) {
            lore.add(shiftHint);
            lore.add(" ");
        }
        lore.add("§7花费: §3§l" + gameManager.getEconomy().formatPrice(ShopPurchase.priceFor(gamePlayer, item)));

        setItem(displaySlot,
                new ItemBuilder().setItemStack(ShopPurchase.displayIcon(gamePlayer, item))
                        .setDisplayName("§c" + item.getName())
                        .setLores(lore)
                        .getItem(),
                new NewGUIAction(0, event -> {
                    if (event.isShiftClick()) {
                        onShiftClick.run();
                        return;
                    }
                    if (ShopPurchase.buy(gamePlayer, item) && item.getKind().isEquipment()) {
                        new ItemShopGUI(gamePlayer, categoryIndex, gameManager).open();
                    }
                }, false));
    }
}
