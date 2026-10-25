package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.shop.QuickBuy;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.shop.ShopItem;
import cc.azuramc.bedwars.shop.ShopPurchase;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class QuickBuyEditGUI extends CustomGUI {

    public QuickBuyEditGUI(GamePlayer gamePlayer, GameManager gameManager, String ref) {
        super(gamePlayer, "§8添加物品到快捷购买", 54);

        ShopCatalog catalog = ShopCatalog.get();
        ShopItem adding = Objects.requireNonNull(catalog.find(ref));
        setItem(4, new ItemBuilder()
                        .setItemStack(ShopPurchase.displayIcon(gamePlayer, adding))
                        .setDisplayName("§c" + adding.getName())
                        .setLores(" ", "§e正在添加物品到快捷购买菜单..")
                        .getItem(),
                new GUIAction(0, () -> {
                }, false));

        for (int i = 0; i < 54; i++) {
            boolean border = i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8;
            if (border && i != 4) {
                setItem(i, XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), new GUIAction(0, () -> {
                }, false));
            }
        }

        PlayerData playerData = gamePlayer.getPlayerData();
        Map<Integer, String> layout = QuickBuy.load(playerData);
        List<Integer> slots = catalog.getQuickBuySlots();
        for (int i = 0; i < slots.size(); i++) {
            int index = i;
            ShopItem custom = QuickBuy.customItem(catalog, layout, index);
            ShopItem fallback = index < catalog.getQuickBuyDefaults().size() ? catalog.getQuickBuyDefaults().get(index) : null;

            ItemStack icon;
            if (custom != null) {
                icon = new ItemBuilder().setItemStack(ShopPurchase.displayIcon(gamePlayer, custom))
                        .setDisplayName("§c" + custom.getName())
                        .setLores("§e点击替换为当前物品")
                        .getItem();
            } else if (fallback != null) {
                icon = new ItemBuilder().setItemStack(ShopPurchase.displayIcon(gamePlayer, fallback))
                        .setDisplayName("§a" + fallback.getName() + " §7(默认)")
                        .setLores("§7这是默认快捷购买物品", "§e点击替换为当前选择的物品")
                        .getItem();
            } else {
                icon = new ItemBuilder().setItemStack(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                        .setDisplayName("§c空闲的槽位")
                        .setLores("§e点击设置该位置为当前物品")
                        .getItem();
            }

            setItem(slots.get(index), icon, new GUIAction(0, () -> {
                QuickBuy.set(playerData, index, ref);
                if (XSound.UI_BUTTON_CLICK.get() != null) {
                    gamePlayer.playSound(XSound.UI_BUTTON_CLICK.get(), 1, 10F);
                }
                new ItemShopGUI(gamePlayer, 0, gameManager).open();
            }, false));
        }
    }
}
