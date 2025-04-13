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
public class BlockShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public BlockShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.TERRACOTTA()).setLores("§e点击查看！").getItem(), "§a方块", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WHITE_WOOL()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(MaterialWrapper.IRON_INGOT(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WHITE_WOOL()).setAmount(4).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).getItem(), "火速羊毛", ColorType.COLOR, new PriceCost(MaterialWrapper.IRON_INGOT(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.TERRACOTTA()).setAmount(16).getItem(), "硬化粘土", ColorType.COLOR, new PriceCost(MaterialWrapper.IRON_INGOT(), 12, 12)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.GLASS()).setAmount(4).getItem(), "防爆玻璃", ColorType.COLOR, new PriceCost(MaterialWrapper.IRON_INGOT(), 12, 12)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.END_STONE()).setAmount(12).getItem(), "末地石", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 24, 24)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.LADDER()).setAmount(16).getItem(), "梯子", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.SIGN()).setAmount(8).getItem(), "告示牌", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 2, 2)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.OAK_PLANKS()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.SLIME_BLOCK()).setAmount(4).getItem(), "史莱姆块", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 2, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.COBWEB()).setAmount(2).getItem(), "蜘蛛网", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.OBSIDIAN()).setAmount(4).getItem(), "黑曜石", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 4, 400)));
    }

}
