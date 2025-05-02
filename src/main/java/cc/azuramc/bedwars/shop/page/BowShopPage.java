package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import org.bukkit.Material;
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
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BOW()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a远程武器", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(Material.ARROW).setAmount(8).getItem(), "箭 x8", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 2, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BOW()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 12, 30)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BOW()).addEnchant(EnchantmentWrapper.ARROW_DAMAGE(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I）", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 24, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BOW()).addEnchant(EnchantmentWrapper.ARROW_DAMAGE(), 1).addEnchant(EnchantmentWrapper.KNOCKBACK(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I,冲击I）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 6, 600)));
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
