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
public class BlockShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public BlockShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.TERRACOTTA.get()).setLores("§e点击查看！").getItem(), "§a方块", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(XMaterial.IRON_INGOT.get(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(4).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).getItem(), "火速羊毛", ColorType.COLOR, new PriceCost(XMaterial.IRON_INGOT.get(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.TERRACOTTA.get()).setAmount(16).getItem(), "硬化粘土", ColorType.COLOR, new PriceCost(XMaterial.IRON_INGOT.get(), 12, 12)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GLASS.get()).setAmount(4).getItem(), "防爆玻璃", ColorType.COLOR, new PriceCost(XMaterial.IRON_INGOT.get(), 12, 12)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.END_STONE.get()).setAmount(12).getItem(), "末地石", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 24, 24)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.LADDER.get()).setAmount(16).getItem(), "梯子", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.OAK_SIGN.get()).setAmount(8).getItem(), "告示牌", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 2, 2)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.OAK_PLANKS.get()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.SLIME_BLOCK.get()).setAmount(4).getItem(), "史莱姆块", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 2, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.COBWEB.get()).setAmount(2).getItem(), "蜘蛛网", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.OBSIDIAN.get()).setAmount(4).getItem(), "黑曜石", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 4, 400)));
    }

}
