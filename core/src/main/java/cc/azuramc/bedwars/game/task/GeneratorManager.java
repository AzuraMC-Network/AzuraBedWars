package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.task.generator.ResourceGenerator;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class GeneratorManager {

    private final GameManager gameManager;
    private final Map<String, BukkitRunnable> tasks;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.tasks = new HashMap<>();
    }

    public void initGeneratorTasks() {
        ResourceGenerator iron = null;
        if (XMaterial.IRON_INGOT.get() != null) {
            iron = new ResourceGenerator(
                    gameManager,
                    "铁锭",
                    XMaterial.IRON_INGOT.get(),
                    MapData.DropType.BASE,
                    48
            );
        }
        addTask(iron, 20L);

        ResourceGenerator gold = null;
        if (XMaterial.GOLD_INGOT.get() != null) {
            gold = new ResourceGenerator(
                    gameManager,
                    "金锭",
                    XMaterial.GOLD_INGOT.get(),
                    MapData.DropType.BASE,
                    8
            );
        }
        addTask(gold, 20L * 3L);

        ResourceGenerator diamond = null;
        if (XMaterial.DIAMOND.get() != null) {
            diamond = new ResourceGenerator(
                    gameManager,
                    "钻石",
                    XMaterial.DIAMOND.get(),
                    MapData.DropType.DIAMOND,
                    4
            );
        }
        addTask(diamond, 20L * 35L);

        ResourceGenerator emerald = null;
        if (XMaterial.EMERALD.get() != null) {
            emerald = new ResourceGenerator(
                    gameManager,
                    "绿宝石",
                    XMaterial.EMERALD.get(),
                    MapData.DropType.EMERALD,
                    2
            );
        }
        addTask(emerald, 20L * 60L);
    }
    
    /**
     * 添加任务，指定执行间隔
     *
     * @param task 任务实例
     * @param interval 执行间隔（刻）
     */
    public void addTask(ResourceGenerator task, long interval) {
        if (task == null) {
            return;
        }

        String taskName = task.getTaskName();
        // 如果已存在同名任务，取消旧任务
        if (tasks.containsKey(taskName)) {
            tasks.get(taskName).cancel();
        }

        // 添加新任务
        tasks.put(taskName, task);
        task.setCurrentTask(task);
        
        // 启动任务
        task.runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
    }

}
