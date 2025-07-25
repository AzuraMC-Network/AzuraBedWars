package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class GeneratorManager {

    private final GameManager gameManager;
    private Map<String, BukkitRunnable> tasks;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.tasks = new HashMap<>();
    }

    /**
     * 添加任务，使用默认间隔（每秒一次）
     * 
     * @param taskName 任务名称
     * @param task 任务实例
     */
    public void addTask(String taskName, BukkitRunnable task) {
        addTask(taskName, task, 20L); // 默认每秒执行一次
    }
    
    /**
     * 添加任务，指定执行间隔
     * 
     * @param taskName 任务名称
     * @param task 任务实例
     * @param interval 执行间隔（刻）
     */
    public void addTask(String taskName, BukkitRunnable task, long interval) {
        // 如果已存在同名任务，取消旧任务
        if (tasks.containsKey(taskName)) {
            tasks.get(taskName).cancel();
        }

        // 添加新任务
        tasks.put(taskName, task);
        
        // 如果任务是ResourceGenerator类型，设置当前任务实例
        if (task instanceof ResourceGenerator) {
            ((ResourceGenerator) task).setCurrentTask(task);
        }
        
        // 启动任务
        task.runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
    }

}
