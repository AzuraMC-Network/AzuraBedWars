package cc.azuramc.bedwars.api.nms;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Version specific code, implemented once per nms module
 *
 * @author an5w1r@163.com
 */
@ApiStatus.Internal
public interface NMSAccess {

    @NotNull
    Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector);

    /**
     * Goals of a mob, used to replace its vanilla AI
     *
     * @return null when the server version has no nms module
     */
    @Nullable
    MobGoals getGoals(@NotNull Creature creature);

    /**
     * Bukkit only added {@link LivingEntity#setAI} in 1.9
     */
    default void setAI(@NotNull LivingEntity entity, boolean ai) {
        entity.setAI(ai);
    }

    /**
     * @param hide whether to hide the unbreakable tooltip
     */
    @NotNull
    default ItemStack setItemUnbreakable(@NotNull ItemStack itemStack, boolean unbreakable, boolean hide) {
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

    /**
     * Recolors a wool block by its legacy data value, legacy modules call {@code setData} instead
     */
    default void setWoolBlockData(@NotNull Block block, byte data) {
        Material type = block.getType();
        if (type.name().toUpperCase().contains("WOOL")) {
            Material newType = Material.getMaterial(woolColorName(data) + "_WOOL");
            if (newType != null) {
                block.setType(newType);
            }
        }
    }

    /**
     * @return the color name of the low 4 bits of a legacy wool data value
     */
    @NotNull
    static String woolColorName(byte data) {
        switch (data & 0xF) {
            case 1:
                return "ORANGE";
            case 2:
                return "MAGENTA";
            case 3:
                return "LIGHT_BLUE";
            case 4:
                return "YELLOW";
            case 5:
                return "LIME";
            case 6:
                return "PINK";
            case 7:
                return "GRAY";
            case 8:
                return "LIGHT_GRAY";
            case 9:
                return "CYAN";
            case 10:
                return "PURPLE";
            case 11:
                return "BLUE";
            case 12:
                return "BROWN";
            case 13:
                return "GREEN";
            case 14:
                return "RED";
            case 15:
                return "BLACK";
            default:
                return "WHITE";
        }
    }
}
