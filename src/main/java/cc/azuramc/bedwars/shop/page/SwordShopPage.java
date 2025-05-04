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
public class SwordShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public SwordShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.GOLDEN_SWORD.get()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a近战武器", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.STONE_SWORD.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.IRON_SWORD.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 7, 35)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.DIAMOND_SWORD.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "钻石剑", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 4, 400)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.STICK.get()).addEnchant(EnchantmentWrapper.KNOCKBACK(), 1).getItem(), "击退棒", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 10, 150)));
    }

}
