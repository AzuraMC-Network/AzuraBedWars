package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.compat.util.WoolUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * @author an5w1r@163.com
 */
public interface NMSAccess {
    Fireball setFireballDirection(Fireball fireball, Vector vector);

    LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health);

    LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health);

    default ItemStack setItemUnbreakable(ItemStack itemStack, boolean unbreakable, boolean hide) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            if (hide) {
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            } else {
                meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    default void setWoolBlockData(Block block, byte data) {
        Material type = block.getType();
        if (type.name().toUpperCase().contains("WOOL")) {
            String colorName = WoolUtil.getColorNameFromData(data);
            Material newType = Material.getMaterial(colorName + "_WOOL");
            if (newType != null) {
                block.setType(newType);
            }
        }
    }
}
