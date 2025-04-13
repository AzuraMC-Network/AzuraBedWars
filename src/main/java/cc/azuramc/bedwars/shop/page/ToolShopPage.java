package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.model.ColorType;
import cc.azuramc.bedwars.shop.model.ShopItemType;
import cc.azuramc.bedwars.shop.model.PriceCost;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ToolShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public ToolShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.STONE_PICKAXE()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a工具", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WOODEN_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem(), "稿子", ColorType.PICKAXE, new PriceCost(MaterialWrapper.IRON_INGOT(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WOODEN_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem(), "斧子", ColorType.AXE, new PriceCost(MaterialWrapper.IRON_INGOT(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.SHEARS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "剪刀", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 30, 30)));
    }

}
