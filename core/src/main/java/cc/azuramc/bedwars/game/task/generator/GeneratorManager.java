package cc.azuramc.bedwars.game.task.generator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.TaskConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author An5w1r@163.com
 */
public class GeneratorManager {

    private final GameManager gameManager;
    private final Map<String, BukkitRunnable> tasks;
    private final int MAX_IRON_STACK_LEVEL_1;
    private final int MAX_GOLD_STACK_LEVEL_1;
    private final int MAX_DIAMOND_STACK_LEVEL_1;
    private final int MAX_EMERALD_STACK_LEVEL_1;
    private final int MAX_IRON_STACK_LEVEL_2;
    private final int MAX_GOLD_STACK_LEVEL_2;
    private final int MAX_DIAMOND_STACK_LEVEL_2;
    private final int MAX_EMERALD_STACK_LEVEL_2;
    private final int MAX_IRON_STACK_LEVEL_3;
    private final int MAX_GOLD_STACK_LEVEL_3;
    private final int MAX_DIAMOND_STACK_LEVEL_3;
    private final int MAX_EMERALD_STACK_LEVEL_3;
    private final float NAME_DISPLAY_HEIGHT;
    private final float RESOURCE_TYPE_HEIGHT;
    private final float LEVEL_DISPLAY_HEIGHT;
    private final String DIAMOND_GENERATOR_NAME;
    private final String EMERALD_GENERATOR_NAME;
    private final String TIME_REMAINING_FORMAT;
    private final String DIAMOND_NAME;
    private final String EMERALD_NAME;
    private final String LEVEL_I;
    private final String LEVEL_II;
    private final String LEVEL_III;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.tasks = new HashMap<>();
        TaskConfig.GeneratorConfig config = AzuraBedWars.getInstance().getTaskConfig().getGenerator();
        MAX_IRON_STACK_LEVEL_1 = config.getMaxIronStackLevel1();
        MAX_GOLD_STACK_LEVEL_1 = config.getMaxGoldStackLevel1();
        MAX_DIAMOND_STACK_LEVEL_1 = config.getMaxDiamondStackLevel1();
        MAX_EMERALD_STACK_LEVEL_1 = config.getMaxEmeraldStackLevel1();
        MAX_IRON_STACK_LEVEL_2 = config.getMaxIronStackLevel2();
        MAX_GOLD_STACK_LEVEL_2 = config.getMaxGoldStackLevel2();
        MAX_DIAMOND_STACK_LEVEL_2 = config.getMaxDiamondStackLevel2();
        MAX_EMERALD_STACK_LEVEL_2 = config.getMaxEmeraldStackLevel2();
        MAX_IRON_STACK_LEVEL_3 = config.getMaxIronStackLevel3();
        MAX_GOLD_STACK_LEVEL_3 = config.getMaxGoldStackLevel3();
        MAX_DIAMOND_STACK_LEVEL_3 = config.getMaxDiamondStackLevel3();
        MAX_EMERALD_STACK_LEVEL_3 = config.getMaxEmeraldStackLevel3();
        MessageConfig.Generator messageConfig = AzuraBedWars.getInstance().getMessageConfig().getGenerator();
        NAME_DISPLAY_HEIGHT = config.getNameDisplayHeight();
        RESOURCE_TYPE_HEIGHT = config.getResourceTypeHeight();
        LEVEL_DISPLAY_HEIGHT = config.getLevelDisplayHeight();
        DIAMOND_GENERATOR_NAME = messageConfig.getDiamondGeneratorName();
        EMERALD_GENERATOR_NAME = messageConfig.getEmeraldGeneratorName();
        TIME_REMAINING_FORMAT = messageConfig.getTimeRemainingFormat();
        DIAMOND_NAME = messageConfig.getDiamondName();
        EMERALD_NAME = messageConfig.getEmeraldName();
        LEVEL_I = messageConfig.getLevelI();
        LEVEL_II = messageConfig.getLevelII();
        LEVEL_III = messageConfig.getLevelIII();
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
        task.setInterval(interval);
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

    public ResourceGenerator getGenerator(String name) {
        BukkitRunnable task = tasks.get(name);
        if (task instanceof ResourceGenerator) {
            return (ResourceGenerator) task;
        }
        return null;
    }
    public int getMaxStackForResource(String resource, int level) {
        switch (resource) {
            case "铁锭":
                if (level == 1) return MAX_IRON_STACK_LEVEL_1;
                if (level == 2) return MAX_IRON_STACK_LEVEL_2;
                if (level == 3) return MAX_IRON_STACK_LEVEL_3;
                break;
            case "金锭":
                if (level == 1) return MAX_GOLD_STACK_LEVEL_1;
                if (level == 2) return MAX_GOLD_STACK_LEVEL_2;
                if (level == 3) return MAX_GOLD_STACK_LEVEL_3;
                break;
            case "钻石":
                if (level == 1) return MAX_DIAMOND_STACK_LEVEL_1;
                if (level == 2) return MAX_DIAMOND_STACK_LEVEL_2;
                if (level == 3) return MAX_DIAMOND_STACK_LEVEL_3;
                break;
            case "绿宝石":
                if (level == 1) return MAX_EMERALD_STACK_LEVEL_1;
                if (level == 2) return MAX_EMERALD_STACK_LEVEL_2;
                if (level == 3) return MAX_EMERALD_STACK_LEVEL_3;
                break;
        }
        return 0;
    }
    public void initDisplayUpdaters() {
        initResourceDisplayUpdater("钻石", DIAMOND_GENERATOR_NAME, gameManager.getArmorStand().keySet(), DIAMOND_NAME);
        initResourceDisplayUpdater("绿宝石", EMERALD_GENERATOR_NAME, gameManager.getArmorSande().keySet(), EMERALD_NAME);
    }
    private void updateResourceDisplay(Set<ArmorStand> armorStands, ResourceGenerator generator, String resourceDisplayName) {
        Iterator<ArmorStand> iterator = armorStands.iterator();
        while (iterator.hasNext()) {
            ArmorStand armorStand = iterator.next();
            if (armorStand == null || !armorStand.isValid()) {
                iterator.remove();
                continue;
            }
            Location location = armorStand.getLocation();
            if (location.getWorld() == null || !location.getChunk().isLoaded()) {
                continue;
            }
            if (armorStand.getFallDistance() == NAME_DISPLAY_HEIGHT) {
                int timeRemaining = generator.getSecondsToNextDrop();
                String displayText = String.format(TIME_REMAINING_FORMAT, timeRemaining);
                armorStand.setCustomName(displayText);
            }
            if (armorStand.getFallDistance() == RESOURCE_TYPE_HEIGHT) {
                armorStand.setCustomName(resourceDisplayName);
            }
            if (armorStand.getFallDistance() == LEVEL_DISPLAY_HEIGHT) {
                int level = generator.getLevel();
                String levelDisplay = level <= 1 ? LEVEL_I : (level == 2 ? LEVEL_II : LEVEL_III);
                armorStand.setCustomName(levelDisplay);
            }
        }
    }
    public void immediateUpdateDisplay(String generatorName) {
        ResourceGenerator generator = getGenerator(generatorName);
        if (generator == null) return;
        Set<ArmorStand> armorStands;
        String resourceDisplayName;
        if (generatorName.equals(DIAMOND_GENERATOR_NAME)) {
            armorStands = new HashSet<>(gameManager.getArmorStand().keySet());
            resourceDisplayName = DIAMOND_NAME;
        } else if (generatorName.equals(EMERALD_GENERATOR_NAME)) {
            armorStands = new HashSet<>(gameManager.getArmorSande().keySet());
            resourceDisplayName = EMERALD_NAME;
        } else {
            return;
        }
        updateResourceDisplay(armorStands, generator, resourceDisplayName);
    }
    private void initResourceDisplayUpdater(String resourceName, String generatorName, Set<ArmorStand> armorStands, String resourceDisplayName) {
        if (armorStands == null || armorStands.isEmpty()) {
            LoggerUtil.warn("尝试初始化资源显示更新，但盔甲架集合为空: " + generatorName);
            return;
        }
        final Set<ArmorStand> safeArmorStands = new HashSet<>(armorStands);
        final ResourceGenerator generator = getGenerator(resourceName);
        if (generator == null) {
            LoggerUtil.warn("无法找到生成器: " + resourceName);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> updateResourceDisplay(safeArmorStands, generator, resourceDisplayName), 0L, 20L);
    }
}
