package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
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
public class BlockShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public BlockShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.TERRACOTTA()).setLores("§e点击查看！").getItem(), "§a方块", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WHITE_WOOL()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(MaterialUtil.IRON_INGOT(), 4, 4)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WHITE_WOOL()).setAmount(4).addEnchant(EnchantmentUtil.DIG_SPEED(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).getItem(), "火速羊毛", ColorType.COLOR, new PriceCost(MaterialUtil.IRON_INGOT(), 4, 4)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.TERRACOTTA()).setAmount(16).getItem(), "硬化粘土", ColorType.COLOR, new PriceCost(MaterialUtil.IRON_INGOT(), 12, 12)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.GLASS()).setAmount(4).getItem(), "防爆玻璃", ColorType.COLOR, new PriceCost(MaterialUtil.IRON_INGOT(), 12, 12)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.END_STONE()).setAmount(12).getItem(), "末地石", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 24, 24)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.LADDER()).setAmount(16).getItem(), "梯子", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 4, 4)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.SIGN()).setAmount(8).getItem(), "告示牌", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 2, 2)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.OAK_PLANKS()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.SLIME_BLOCK()).setAmount(4).getItem(), "史莱姆块", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 2, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.COBWEB()).setAmount(2).getItem(), "蜘蛛网", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.OBSIDIAN()).setAmount(4).getItem(), "黑曜石", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 4, 400)));
    }

}
