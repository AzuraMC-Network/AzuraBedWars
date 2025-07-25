package cc.azuramc.bedwars.game.task;

/**
 * @author An5w1r@163.com
 */

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.TaskConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import com.cryptomorin.xseries.XMaterial;
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
public class ResourceGenerator extends BukkitRunnable {
    private final Material material;
    private final MapData.DropType dropType;
    private int maxStack;
    private long interval = 20L; // 默认间隔为20刻（1秒）
    private BukkitRunnable currentTask;
    private final GameManager gameManager;

    /**
     * 创建资源生成器
     *
     * @param gameManager 游戏实例
     * @param material 资源物品类型
     * @param dropType 资源生成点类型
     * @param level 初始等级
     */
    public ResourceGenerator(GameManager gameManager, Material material, MapData.DropType dropType, int level) {
        this.gameManager = gameManager;
        this.material = material;
        this.dropType = dropType;
        updateMaxStack(level);

        // 根据资源类型设置默认间隔
        TaskConfig.GeneratorConfig config = AzuraBedWars.getInstance().getTaskConfig().getGenerator();
        if (material.equals(XMaterial.IRON_INGOT.get())) {
            this.interval = config.getIronSpawnInterval();
        } else if (material.equals(XMaterial.GOLD_INGOT.get())) {
            this.interval = config.getGoldSpawnInterval();
        } else if (material.equals(XMaterial.DIAMOND.get())) {
            this.interval = config.getDiamondSpawnInterval();
        } else if (material.equals(XMaterial.EMERALD.get())) {
            this.interval = config.getEmeraldSpawnInterval();
        }
    }

    @Override
    public void run() {
        int currentEventLevel = gameManager.getGameEventManager().currentEvent().getPriority();
        // 确保maxStack始终是最新的
        updateMaxStack(currentEventLevel);

        // 生成资源
        gameManager.getMapData().getDropLocations(dropType).forEach(location -> dropItem(location, material, maxStack));
    }

    /**
     * 更新最大堆叠数量
     *
     * @param level 事件等级
     */
    public void updateMaxStack(int level) {
        if (material.equals(XMaterial.IRON_INGOT.get())) {
            this.maxStack = getMaxIronStack(level);
        } else if (material.equals(XMaterial.GOLD_INGOT.get())) {
            this.maxStack = getMaxGoldStack(level);
        } else if (material.equals(XMaterial.DIAMOND.get())) {
            this.maxStack = getMaxDiamondStack(level);
        } else if (material.equals(XMaterial.EMERALD.get())) {
            this.maxStack = getMaxEmeraldStack(level);
        }
    }

    /**
     * 设置掉落间隔并重新调度任务
     *
     * @param interval 新的掉落间隔（刻）
     */
    public void setInterval(long interval) {
        this.interval = interval;

        // 如果当前任务正在运行，需要取消并重新调度
        if (currentTask != null) {
            currentTask.cancel();

            // 获取任务名称
            String taskName = null;
            MessageConfig.Generator messageConfig = AzuraBedWars.getInstance().getMessageConfig().getGenerator();
            if (material.equals(XMaterial.IRON_INGOT.get())) {
                taskName = messageConfig.getIronGeneratorName();
            } else if (material.equals(XMaterial.GOLD_INGOT.get())) {
                taskName = messageConfig.getGoldGeneratorName();
            } else if (material.equals(XMaterial.DIAMOND.get())) {
                taskName = messageConfig.getDiamondGeneratorName();
            } else if (material.equals(XMaterial.EMERALD.get())) {
                taskName = messageConfig.getEmeraldGeneratorName();
            }

            if (taskName != null) {
                // 重新注册任务，使用新的间隔
                gameManager.getGeneratorManager().addTask(taskName, this, interval);
            }
        }
    }

    /**
     * 根据当前事件等级获取铁锭最大堆叠数量
     *
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    public int getMaxIronStack(int eventLevel) {
        if (eventLevel <= 1) {
            return 48;
        } else if (eventLevel == 2) {
            return 48;
        } else {
            return 48;
        }
    }

    /**
     * 根据当前事件等级获取金锭最大堆叠数量
     *
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    public int getMaxGoldStack(int eventLevel) {
        if (eventLevel <= 1) {
            return 8;
        } else if (eventLevel == 2) {
            return 8;
        } else {
            return 8;
        }
    }

    /**
     * 根据当前事件等级获取钻石最大堆叠数量
     *
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    public int getMaxDiamondStack(int eventLevel) {
        if (eventLevel <= 1) {
            return 4;
        } else if (eventLevel == 2) {
            return 6;
        } else {
            return 8;
        }
    }

    /**
     * 根据当前事件等级获取绿宝石最大堆叠数量
     *
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    public int getMaxEmeraldStack(int eventLevel) {
        if (eventLevel <= 1) {
            return 2;
        } else if (eventLevel == 2) {
            return 4;
        } else {
            return 8;
        }
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