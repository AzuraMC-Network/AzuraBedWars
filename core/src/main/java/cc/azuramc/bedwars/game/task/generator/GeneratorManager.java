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

import java.util.*;

/**
 * @author An5w1r@163.com
 */
public class GeneratorManager {

    private final GameManager gameManager;
    private final Map<String, PublicResourceGenerator> publicResourceGeneratorMap;
    private final Map<String, PrivateResourceGenerator> privateResourceGeneratorMap;

    private final int MAX_DIAMOND_STACK_LEVEL_1;
    private final int MAX_EMERALD_STACK_LEVEL_1;
    private final int MAX_DIAMOND_STACK_LEVEL_2;
    private final int MAX_EMERALD_STACK_LEVEL_2;
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
        this.publicResourceGeneratorMap = new HashMap<>();
        this.privateResourceGeneratorMap = new HashMap<>();

        TaskConfig.GeneratorConfig config = AzuraBedWars.getInstance().getTaskConfig().getGenerator();
        MAX_DIAMOND_STACK_LEVEL_1 = config.getMaxDiamondStackLevel1();
        MAX_EMERALD_STACK_LEVEL_1 = config.getMaxEmeraldStackLevel1();
        MAX_DIAMOND_STACK_LEVEL_2 = config.getMaxDiamondStackLevel2();
        MAX_EMERALD_STACK_LEVEL_2 = config.getMaxEmeraldStackLevel2();
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
        for (Location dropLocation : gameManager.getMapData().getDropLocations(MapData.DropType.BASE)) {
            PrivateResourceGenerator iron = null;
            if (XMaterial.IRON_INGOT.get() != null) {
                iron = new PrivateResourceGenerator(
                        gameManager,
                        "铁锭" + dropLocation.toString(),
                        dropLocation,
                        XMaterial.IRON_INGOT.get(),
                        48
                );
            }
            addPrivateResourceTask(iron, 20L);

            PrivateResourceGenerator gold = null;
            if (XMaterial.GOLD_INGOT.get() != null) {
                gold = new PrivateResourceGenerator(
                        gameManager,
                        "金锭" + dropLocation.toString(),
                        dropLocation,
                        XMaterial.GOLD_INGOT.get(),
                        8
                );
            }
            addPrivateResourceTask(gold, 20L * 3L);
        }

        PublicResourceGenerator diamond = null;
        if (XMaterial.DIAMOND.get() != null) {
            diamond = new PublicResourceGenerator(
                    gameManager,
                    "钻石",
                    XMaterial.DIAMOND.get(),
                    MapData.DropType.DIAMOND,
                    4
            );
        }
        addPublicResourceTask(diamond, 20L * 35L);

        PublicResourceGenerator emerald = null;
        if (XMaterial.EMERALD.get() != null) {
            emerald = new PublicResourceGenerator(
                    gameManager,
                    "绿宝石",
                    XMaterial.EMERALD.get(),
                    MapData.DropType.EMERALD,
                    2
            );
        }
        addPublicResourceTask(emerald, 20L * 60L);
    }

    /**
     * 添加私有资源任务，指定执行间隔
     *
     * @param task 任务实例
     * @param interval 执行间隔（刻）
     */
    public void addPrivateResourceTask(PrivateResourceGenerator task, long interval) {
        if (task == null) {
            return;
        }
        task.setInterval(interval);
        String taskName = task.getTaskName();
        // 如果已存在同名任务，取消旧任务
        if (privateResourceGeneratorMap.containsKey(taskName)) {
            privateResourceGeneratorMap.get(taskName).cancel();
        }
        // 添加新任务
        privateResourceGeneratorMap.put(taskName, task);
        task.setCurrentTask(task);
        // 启动任务
        task.runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
    }

    /**
     * 添加公开资源任务，指定执行间隔
     *
     * @param task 任务实例
     * @param interval 执行间隔（刻）
     */
    public void addPublicResourceTask(PublicResourceGenerator task, long interval) {
        if (task == null) {
            return;
        }
        task.setInterval(interval);
        String taskName = task.getTaskName();
        // 如果已存在同名任务，取消旧任务
        if (publicResourceGeneratorMap.containsKey(taskName)) {
            publicResourceGeneratorMap.get(taskName).cancel();
        }
        // 添加新任务
        publicResourceGeneratorMap.put(taskName, task);
        task.setCurrentTask(task);
        // 启动任务
        task.runTaskTimer(AzuraBedWars.getInstance(), 0L, interval);
    }

    public PublicResourceGenerator getPublicResourceGenerator(String name) {
        return publicResourceGeneratorMap.get(name);
    }

    public int getMaxStackForResource(String resource, int level) {
        switch (resource) {
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
    private void updateResourceDisplay(Set<ArmorStand> armorStands, PublicResourceGenerator generator, String resourceDisplayName) {
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
        PublicResourceGenerator generator = getPublicResourceGenerator(generatorName);
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
        final PublicResourceGenerator generator = getPublicResourceGenerator(resourceName);
        if (generator == null) {
            LoggerUtil.warn("无法找到生成器: " + resourceName);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> updateResourceDisplay(safeArmorStands, generator, resourceDisplayName), 0L, 20L);
    }
}
