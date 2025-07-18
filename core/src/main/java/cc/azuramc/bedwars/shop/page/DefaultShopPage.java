package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
@Getter
public class DefaultShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public DefaultShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.NETHER_STAR.get()).setLores("§e点击查看！").getItem(), "§b快速购买", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(16).getItem(), "羊毛", ColorType.COLOR, new PriceCost(XMaterial.IRON_INGOT.get(), 4, 4)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.STONE_SWORD.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "石剑", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.CHAINMAIL_BOOTS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "锁链装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 40, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.GOLDEN_APPLE.get()).getItem(), "金苹果", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 3, 15)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.BOW.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "弓", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(Objects.requireNonNull(XPotion.SPEED.get()), 600, 1)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.TNT.get()).getItem(), "TNT", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 4, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.OAK_PLANKS.get()).setAmount(16).getItem(), "橡木板", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 4, 20)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.IRON_SWORD.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁剑", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 7, 35)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.IRON_BOOTS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.FIRE_CHARGE.get()).getItem(), "火球", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 50, 50)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.ARROW.get()).setAmount(8).getItem(), "箭", ColorType.NONE, new PriceCost(XMaterial.IRON_INGOT.get(), 8, 10)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(Objects.requireNonNull(XPotion.JUMP_BOOST.get()), 600, 1)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.WATER_BUCKET.get()).getItem(), "水桶", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
    }
}