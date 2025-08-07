package cc.azuramc.bedwars.game.task.generator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * 自定义公共资源生成器类。
 * 可动态修改掉落间隔和最大堆叠量，用于地图中的公共掉落点。
 *
 * @author An5w1r@163.com
 */
@Getter
@Setter
public class PublicResourceGenerator {
    private String taskName;
    private final Material material;
    private final MapData.DropType dropType;
    private int maxStack;
    private long interval = 20L;
    private int level = 1;
    private long lastDropTime = 0;
    private BukkitTask currentTask;
    private final GameManager gameManager;

    /**
     * 创建资源生成器
     *
     * @param gameManager 游戏实例
     * @param material    资源物品类型
     * @param dropType    资源生成点类型
     * @param maxStack    最大堆叠数量
     */
    public PublicResourceGenerator(GameManager gameManager, String taskName, Material material, MapData.DropType dropType, int maxStack) {
        this.gameManager = gameManager;
        this.taskName = taskName;
        this.material = material;
        this.dropType = dropType;
        this.maxStack = maxStack;
        this.lastDropTime = System.currentTimeMillis();
    }

    /**
     * 启动任务
     */
    public void startTask() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        currentTask = new GeneratorTask().runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
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

    public int getSecondsToNextDrop() {
        long now = System.currentTimeMillis();
        // 1 tick = 50ms
        long intervalMs = interval * 50L;
        if (intervalMs <= 0) return 0;
        long timeSinceLast = now - lastDropTime;
        long timeInCurrent = timeSinceLast % intervalMs;
        long timeToNext = intervalMs - timeInCurrent;
        return (int) Math.ceil((double) timeToNext / 1000);
    }

    /**
     * 重新启动任务以应用新的间隔
     */
    public void restartTask() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        // 重置最后掉落时间，确保倒计时从新间隔开始
        lastDropTime = System.currentTimeMillis();
        currentTask = new GeneratorTask().runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
    }

    /**
     * 内部任务类，用于处理资源生成
     */
    private class GeneratorTask extends BukkitRunnable {
        @Override
        public void run() {
            gameManager.getMapData().getDropLocations(dropType).forEach(location -> dropItem(location, material, getMaxStack()));
            lastDropTime = System.currentTimeMillis();
            gameManager.getGeneratorManager().immediateUpdateDisplay(taskName);
        }
    }
}
