package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.utils.PotionEffectUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;

import java.util.LinkedList;
import java.util.List;

@Getter
public class DefaultShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public DefaultShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.NETHER_STAR()).setLores("§e点击查看！").getItem(), "§b快速购买", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WHITE_WOOL()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(MaterialUtil.IRON_INGOT(), 4, 4)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.STONE_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.CHAINMAIL_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "锁链装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 40, 40)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_APPLE()).getItem(), "金苹果", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 3, 15)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BOW()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 60)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectUtil.SPEED(), 600, 1)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.TNT()).getItem(), "TNT", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 4, 40)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.OAK_PLANKS()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 4, 20)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.IRON_SWORD()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 7, 35)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.IRON_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 60)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.FIREBALL()).getItem(), "火球", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 50, 50)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.ARROW()).setAmount(8).getItem(), "箭", ColorType.NONE, new PriceCost(MaterialUtil.IRON_INGOT(), 8, 10)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectUtil.JUMP_BOOST(), 600, 1)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.WATER_BUCKET()).getItem(), "水桶", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
    }
}
