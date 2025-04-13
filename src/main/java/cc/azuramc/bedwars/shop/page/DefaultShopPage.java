package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.PotionEffectWrapper;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.model.ColorType;
import cc.azuramc.bedwars.shop.model.ShopItemType;
import cc.azuramc.bedwars.shop.model.PriceCost;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;

import java.util.LinkedList;
import java.util.List;

@Getter
public class DefaultShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public DefaultShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.NETHER_STAR()).setLores("§e点击查看！").getItem(), "§b快速购买", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WHITE_WOOL()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(MaterialWrapper.IRON_INGOT(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.STONE_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.CHAINMAIL_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "锁链装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 40, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.GOLDEN_APPLE()).getItem(), "金苹果", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 3, 15)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BOW()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 12, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.POTION()).setPotionData(new PotionEffect(PotionEffectWrapper.SPEED(), 600, 1)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.TNT()).getItem(), "TNT", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 4, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.OAK_PLANKS()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.IRON_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 7, 35)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.IRON_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(MaterialWrapper.GOLD_INGOT(), 12, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.FIREBALL()).getItem(), "火球", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 50, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.ARROW()).setAmount(8).getItem(), "箭", ColorType.NONE, new PriceCost(MaterialWrapper.IRON_INGOT(), 8, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.POTION()).setPotionData(new PotionEffect(PotionEffectWrapper.JUMP_BOOST(), 600, 1)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.WATER_BUCKET()).getItem(), "水桶", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
    }
}
