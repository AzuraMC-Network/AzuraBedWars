package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.PotionEffectWrapper;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
@Getter
public class PotionShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public PotionShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(XMaterial.BREWING_STAND.get()).setLores("§e点击查看！").getItem(), "§a药水", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(PotionEffectType.SPEED, 600, 2)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(Objects.requireNonNull(PotionEffectWrapper.JUMP_BOOST()), 600, 5)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1)).setDisplayName("§b隐身药水§7（30秒）").getItem(), "隐身药水§7（30秒）", ColorType.NONE, new PriceCost(XMaterial.EMERALD.get(), 2, 200)));
    }

}