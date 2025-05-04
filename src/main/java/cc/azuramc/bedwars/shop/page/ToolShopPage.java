package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
public class ToolShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public ToolShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.STONE_PICKAXE.get()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a工具", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WOODEN_PICKAXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem(), "稿子", ColorType.PICKAXE, new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WOODEN_AXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem(), "斧子", ColorType.AXE, new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.SHEARS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "剪刀", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 30, 30)));
    }

}
