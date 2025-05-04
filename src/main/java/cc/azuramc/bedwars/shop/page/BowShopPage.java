package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class BowShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public BowShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.BOW.get()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a远程武器", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.ARROW.get()).setAmount(8).getItem(), "箭 x8", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 2, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.BOW.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 30)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.BOW.get()).addEnchant(EnchantmentWrapper.ARROW_DAMAGE(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I）", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 24, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.BOW.get()).addEnchant(EnchantmentWrapper.ARROW_DAMAGE(), 1).addEnchant(EnchantmentWrapper.KNOCKBACK(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I,冲击I）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 6, 600)));
    }

    @Override
    public ShopItemType getMainShopItem() {
        return mainShopItem;
    }

    @Override
    public List<ShopItemType> getShopItems() {
        return shopItems;
    }
}
