package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.compat.potioneffect.PotionEffectUtil;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
public class PotionShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public PotionShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BREWING_STAND()).setLores("§e点击查看！").getItem(), "§a药水", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectType.SPEED, 600, 2)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(Objects.requireNonNull(PotionEffectUtil.JUMP_BOOST()), 600, 5)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1)).setDisplayName("§b隐身药水§7（30秒）").getItem(), "隐身药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 2, 200)));
    }

}
