package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ToolShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public ToolShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.STONE_PICKAXE()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a工具", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WOODEN_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).getItem(), "稿子", ColorType.PICKAXE, new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WOODEN_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).getItem(), "斧子", ColorType.AXE, new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.SHEARS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "剪刀", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 30, 30)));
    }

}
