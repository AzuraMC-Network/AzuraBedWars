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
import org.jetbrains.annotations.NotNull;

/**
 * NMS 版本适配的服务提供接口
 *
 * @author an5w1r@163.com
 */
public interface NMSAccess {

    /**
     * 设置火球的飞行方向
     *
     * @param fireball 火球实体
     * @param vector   方向向量
     * @return 处理后的火球实体
     */
    Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector);

    /**
     * Goals of a mob, used to replace its vanilla AI
     *
     * @return null when the server version has no nms module
     */
    MobGoals getGoals(Creature creature);

    /**
     * Bukkit only added {@link LivingEntity#setAI} in 1.9
     */
    default void setAI(LivingEntity entity, boolean ai) {
        entity.setAI(ai);
    }

    /**
     * 设置物品是否无法破坏（含是否隐藏无法破坏标签）
     * <p>默认实现使用 Bukkit API，必要时版本模块可覆盖。
     *
     * @param itemStack   物品
     * @param unbreakable 是否无法破坏
     * @param hide        是否隐藏标签
     * @return 处理后的物品
     */
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

    /**
     * 按旧版羊毛数据值设置方块颜色
     * <p>默认实现按低 4 位映射颜色名再设置方块类型；低版本模块通常覆盖为直接 {@code setData}。
     *
     * @param block 方块
     * @param data  羊毛数据值
     */
    default void setWoolBlockData(Block block, byte data) {
        Material type = block.getType();
        if (type.name().toUpperCase().contains("WOOL")) {
            Material newType = Material.getMaterial(woolColorName(data) + "_WOOL");
            if (newType != null) {
                block.setType(newType);
            }
        }
    }

    /**
     * 将旧版羊毛数据值（低 4 位）映射为颜色名
     *
     * @param data 数据值
     * @return 颜色名
     */
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
