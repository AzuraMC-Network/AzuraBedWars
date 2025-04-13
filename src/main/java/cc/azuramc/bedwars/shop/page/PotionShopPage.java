package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.shop.model.ShopData;
import cc.azuramc.bedwars.shop.model.ColorType;
import cc.azuramc.bedwars.shop.model.ShopItemType;
import cc.azuramc.bedwars.shop.model.PriceCost;
import cc.azuramc.bedwars.compat.wrapper.PotionEffectWrapper;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
public class PotionShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public PotionShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(MaterialWrapper.BREWING_STAND()).setLores("§e点击查看！").getItem(), "§a药水", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.POTION()).setPotionData(new PotionEffect(PotionEffectType.SPEED, 600, 2)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.POTION()).setPotionData(new PotionEffect(Objects.requireNonNull(PotionEffectWrapper.JUMP_BOOST()), 600, 5)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 1, 100)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(MaterialWrapper.POTION()).setPotionData(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1)).setDisplayName("§b隐身药水§7（30秒）").getItem(), "隐身药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialWrapper.EMERALD(), 2, 200)));
    }

}
