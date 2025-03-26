package cc.azuramc.bedwars.shop.data;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ArmorShop implements ShopData {
    private final ItemType mainShopItem;
    private final List<ItemType> shopItems = new LinkedList<>();

    public ArmorShop() {
        mainShopItem = new ItemType(new ItemBuilderUtil().setType(Material.CHAINMAIL_BOOTS).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a盔甲", ColorType.NONE, null);

        shopItems.add(new ItemType(new ItemBuilderUtil().setType(Material.CHAINMAIL_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "锁链装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.IRON_INGOT, 40, 40)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(Material.IRON_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.GOLD_INGOT, 12, 60)));
        shopItems.add(new ItemType(new ItemBuilderUtil().setType(Material.DIAMOND_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "钻石装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.EMERALD, 6, 600)));
    }

}
