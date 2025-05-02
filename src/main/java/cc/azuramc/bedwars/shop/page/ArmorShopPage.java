package cc.azuramc.bedwars.shop.page;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.PriceCost;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.LinkedList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
public class ArmorShopPage implements ShopData {
    private final ShopItemType mainShopItem;
    private final List<ShopItemType> shopItems = new LinkedList<>();

    public ArmorShopPage() {
        mainShopItem = new ShopItemType(new ItemBuilder().setType(Material.CHAINMAIL_BOOTS).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "§a盔甲", ColorType.NONE, null);

        shopItems.add(new ShopItemType(new ItemBuilder().setType(Material.CHAINMAIL_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "锁链装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.IRON_INGOT, 40, 40)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(Material.IRON_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "铁装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.GOLD_INGOT, 12, 60)));
        shopItems.add(new ShopItemType(new ItemBuilder().setType(Material.DIAMOND_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem(), "钻石装备§7（死亡不掉落）", ColorType.NONE, new PriceCost(Material.EMERALD, 6, 600)));
    }

}
