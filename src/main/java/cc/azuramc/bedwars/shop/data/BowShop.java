package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.utils.EnchantmentUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

public class BowShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public BowShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BOW()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a远程武器", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(Material.ARROW).setAmount(8).getItem(), "箭 x8", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 2, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BOW()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 30)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BOW()).addEnchant(EnchantmentUtil.ARROW_DAMAGE(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I）", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 24, 60)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BOW()).addEnchant(EnchantmentUtil.ARROW_DAMAGE(), 1).addEnchant(EnchantmentUtil.KNOCKBACK(), 1).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓（力量I,冲击I）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 6, 600)));
    }

    @Override
    public ItemType getMainShopItem() {
        return mainShopItem;
    }

    @Override
    public List<ItemType> getShopItems() {
        return shopItems;
    }
}
