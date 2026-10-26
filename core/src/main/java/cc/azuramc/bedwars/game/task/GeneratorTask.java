package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.generator.ResourceHologram;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.packet.ArmorStandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.*;

/**
 * 资源生成计时器
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 同时管理资源点上的盔甲架显示，包括资源名称、等级和下次刷新时间的显示。
 * </p>
 *
 * @author an5w1r@163.com
 */
public class GeneratorTask {
    private final GameManager gameManager;
    private boolean timer;
    private int taskId = -1;

    /**
     * 创建资源生成计时器
     *
     * @param gameManager 当前游戏实例
     */
    public GeneratorTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * 启动资源生成计时器
     * 注册各类资源的生成事件和显示更新
     */
    public void start() {
        if (!this.timer) {
            timer = true;
            startArmorStandUpdateTask();
            gameManager.getGeneratorManager().initGeneratorTasks();
            gameManager.getGeneratorManager().initDisplayUpdaters();
        }
    }

    /**
     * 停止所有计时器任务
     */
    public void stop() {
        if (this.timer) {
            this.timer = false;
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }

    /**
     * 启动盔甲架位置更新任务
     */
    private void startArmorStandUpdateTask() {
        taskId = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance().getPlugin(), () -> {
            try {
                List<ArmorStand> allArmor = new ArrayList<>();
                for (List<ResourceHologram> holograms : gameManager.getResourceHolograms().values()) {
                    for (ResourceHologram hologram : holograms) {
                        if (hologram.getBlock() != null) {
                            allArmor.add(hologram.getBlock());
                        }
                    }
                }

                // 过滤掉无效的盔甲架
                Iterator<ArmorStand> iterator = allArmor.iterator();
                while (iterator.hasNext()) {
                    ArmorStand as = iterator.next();
                    if (as == null || !as.isValid()) {
                        iterator.remove();
                        continue;
                    }

                    try {
                        Location loc = as.getLocation();
                        if (loc.getWorld() != null) {
                            loc.getChunk();
                            if (!loc.getChunk().isLoaded()) {
                                loc.getChunk().load();
                            }
                        }
                        ArmorStandUtil.moveArmorStand(as, as.getLocation().getY());
                    } catch (Exception e) {
                        // 记录异常但继续处理其他盔甲架
                        LoggerUtil.warn("Error while updating a hologram: " + e.getMessage());
                        iterator.remove(); // 移除有问题的盔甲架
                    }
                }
            } catch (Exception e) {
                // 捕获总体异常，确保任务不会终止
                LoggerUtil.warn("Error in the hologram task: " + e.getMessage());
            }
        }, 0L, 1L).getTaskId();
    }
}
