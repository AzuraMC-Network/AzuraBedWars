package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.model.ColorType;
import cc.azuramc.bedwars.shop.model.ShopItemType;
import cc.azuramc.bedwars.shop.model.PriceCost;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

@Getter
public class SwordShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public SwordShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.GOLDEN_SWORD()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a近战武器", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.STONE_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.IRON_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 7, 35)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.DIAMOND_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "钻石剑", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 4, 400)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.STICK()).addEnchant(EnchantmentWrapper.KNOCKBACK(), 1).getItem(), "击退棒", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 10, 150)));
    }

}
