package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.utils.PotionEffectUtil;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;

@Getter
public class PotionShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public PotionShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(MaterialUtil.BREWING_STAND()).setLores("§e点击查看！").getItem(), "§a药水", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectType.SPEED, 600, 1)).setDisplayName("§b速度药水§7（30秒）").getItem(), "速度药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(MaterialUtil.POTION()).setPotionData(new PotionEffect(PotionEffectUtil.JUMP_BOOST(), 600, 1)).setDisplayName("跳跃药水§7（30秒）").getItem(), "跳跃药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 1, 100)));
        //shopItems.add(new ItemType(ItemUtil.makePotion(1, new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1), "§b隐身药水§7（30秒）", null), "隐身药水§7（30秒）", ColorType.NONE, new PriceCost(MaterialUtil.EMERALD(), 2, 200)));
    }

}
