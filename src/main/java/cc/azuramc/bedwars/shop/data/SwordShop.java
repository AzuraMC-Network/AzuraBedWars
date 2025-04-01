package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

@Getter
public class SwordShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public SwordShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_SWORD()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a近战武器", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.STONE_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.IRON_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 7, 35)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.DIAMOND_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "钻石剑", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 4, 400)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.STICK()).addEnchant(EnchantmentUtil.KNOCKBACK(), 1).getItem(), "击退棒", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 10, 150)));
    }

}
