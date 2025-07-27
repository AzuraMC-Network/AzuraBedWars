package cc.azuramc.bedwars.game.task.generator;

/**
 * @author An5w1r@163.com
 */

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * 自定义资源生成器类
 * 可以动态修改最大堆叠数量和掉落物品间隔
 */
@Getter
@Setter
public class PrivateResourceGenerator extends BukkitRunnable {
    private String taskName;
    private Location location;
    private final Material material;
    private int maxStack;
    private long interval = 20L;
    private int level = 1;
    private BukkitRunnable currentTask;
    private final GameManager gameManager;

    /**
     * 创建资源生成器
     *
     * @param gameManager 游戏实例
     * @param material 资源物品类型
     * @param maxStack 最大堆叠数量
     */
    public PrivateResourceGenerator(GameManager gameManager, String taskName, Location location, Material material, int maxStack) {
        this.gameManager = gameManager;
        this.taskName = taskName;
        this.location = location;
        this.material = material;
        this.maxStack = maxStack;
    }

    @Override
    public void run() {
        dropItem(location, material, getMaxStack());
    }

    /**
     * 在指定位置生成物品，但不超过最大堆叠数量
     *
     * @param location 生成位置
     * @param material 物品类型
     * @param maxStack 最大堆叠数量
     */
    public void dropItem(Location location, Material material, int maxStack) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        // 计算当前位置已有的资源数量
        int currentAmount = countNearbyItems(location, material);

        // 如果已达到或超过最大堆叠数量，则不生成
        if (currentAmount >= maxStack) {
            return;
        }

        // 生成物品
        location.getWorld().dropItem(
                location,
                new ItemBuilder()
                        .setType(material)
//                .setDisplayName(ITEM_DISPLAY_NAME)
                        .getItem()
        ).setVelocity(new Vector(0.0D, 0.2D, 0.0D));
    }

    /**
     * 计算指定位置附近特定类型物品的数量
     *
     * @param location 中心位置
     * @param material 物品类型
     * @return 物品总数
     */
    private int countNearbyItems(Location location, Material material) {
        if (location == null || location.getWorld() == null) {
            return 0;
        }

        // 获取周围实体
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(
                location,
                3,
                3,
                3
        );

        // 统计指定类型的物品数量
        int count = 0;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item item) {
                if (item.getItemStack().getType() == material) {
                    count += item.getItemStack().getAmount();
                }
            }
        }

        return count;
    }
}
