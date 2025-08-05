package cc.azuramc.bedwars.game.task.generator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ResourceSpawnConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.*;

/**
 * 资源刷新管理类，负责注册task等操作
 * @author An5w1r@163.com
 */
public class GeneratorManager {

    private static final ResourceSpawnConfig resourceSpawnConfig = AzuraBedWars.getInstance().getResourceSpawnConfig();
    private final GameManager gameManager;
    private final Map<String, PublicResourceGenerator> publicResourceGeneratorMap;
    private final Map<String, PrivateResourceGenerator> privateResourceGeneratorMap;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.publicResourceGeneratorMap = new HashMap<>();
        this.privateResourceGeneratorMap = new HashMap<>();
    }

    public void initGeneratorTasks() {
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            PrivateResourceGenerator iron = null;
            if (XMaterial.IRON_INGOT.get() != null) {
                iron = new PrivateResourceGenerator(
                        gameManager,
                        "铁锭" + gameTeam.getName(),
                        gameTeam.getResourceDropLocation(),
                        XMaterial.IRON_INGOT.get(),
                        48
                );
            }
            addPrivateResourceTask(iron, 20L * 1);

            PrivateResourceGenerator gold = null;
            if (XMaterial.GOLD_INGOT.get() != null) {
                gold = new PrivateResourceGenerator(
                        gameManager,
                        "金锭" + gameTeam.getName(),
                        gameTeam.getResourceDropLocation(),
                        XMaterial.GOLD_INGOT.get(),
                        8
                );
            }
            addPrivateResourceTask(gold, 20L * 3);
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
        addPublicResourceTask(diamond, 20L * 35);

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
        addPublicResourceTask(emerald, 20L * 60);
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
            PrivateResourceGenerator oldTask = privateResourceGeneratorMap.get(taskName);
            if (oldTask.getCurrentTask() != null) {
                oldTask.getCurrentTask().cancel();
            }
        }
        // 添加新任务
        privateResourceGeneratorMap.put(taskName, task);
        // 启动任务
        task.startTask();
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
            PublicResourceGenerator oldTask = publicResourceGeneratorMap.get(taskName);
            if (oldTask.getCurrentTask() != null) {
                oldTask.getCurrentTask().cancel();
            }
        }
        // 添加新任务
        publicResourceGeneratorMap.put(taskName, task);
        // 启动任务
        task.startTask();
    }

    public PublicResourceGenerator getPublicResourceGenerator(String name) {
        return publicResourceGeneratorMap.get(name);
    }

    public PrivateResourceGenerator getPrivateResourceGenerator(String name) {
        return privateResourceGeneratorMap.get(name);
    }

    public int getMaxStackForResource(String resource, int level) {
        switch (resource) {
            case "钻石":
                if (level == 1) return resourceSpawnConfig.getMaxDiamondStackLevel1();
                if (level == 2) return resourceSpawnConfig.getMaxDiamondStackLevel2();
                if (level == 3) return resourceSpawnConfig.getMaxDiamondStackLevel3();
                break;
            case "绿宝石":
                if (level == 1) return resourceSpawnConfig.getMaxDiamondStackLevel1();
                if (level == 2) return resourceSpawnConfig.getMaxDiamondStackLevel2();
                if (level == 3) return resourceSpawnConfig.getMaxDiamondStackLevel3();
                break;
        }
        return 0;
    }

    public void initDisplayUpdaters() {
        initResourceDisplayUpdater("钻石", resourceSpawnConfig.getDiamondGeneratorName(), gameManager.getArmorStand().keySet(), resourceSpawnConfig.getDiamondName());
        initResourceDisplayUpdater("绿宝石", resourceSpawnConfig.getEmeraldGeneratorName(), gameManager.getArmorSande().keySet(), resourceSpawnConfig.getEmeraldName());
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
            if (armorStand.getFallDistance() == resourceSpawnConfig.getNameDisplayHeight()) {
                int timeRemaining = generator.getSecondsToNextDrop();
                String displayText = String.format(resourceSpawnConfig.getTimeRemainingFormat(), timeRemaining);
                armorStand.setCustomName(displayText);
            }
            if (armorStand.getFallDistance() == resourceSpawnConfig.getResourceTypeHeight()) {
                armorStand.setCustomName(resourceDisplayName);
            }
            if (armorStand.getFallDistance() == resourceSpawnConfig.getLevelDisplayHeight()) {
                int level = generator.getLevel();
                String levelDisplay = level <= 1 ? resourceSpawnConfig.getLevelI() : (level == 2 ? resourceSpawnConfig.getLevelII() : resourceSpawnConfig.getLevelIII());
                armorStand.setCustomName(levelDisplay);
            }
        }
    }

    public void immediateUpdateDisplay(String generatorName) {
        PublicResourceGenerator generator = getPublicResourceGenerator(generatorName);
        if (generator == null) return;
        Set<ArmorStand> armorStands;
        String resourceDisplayName;
        if (generatorName.equals(resourceSpawnConfig.getDiamondGeneratorName())) {
            armorStands = new HashSet<>(gameManager.getArmorStand().keySet());
            resourceDisplayName = resourceSpawnConfig.getDiamondName();
        } else if (generatorName.equals(resourceSpawnConfig.getEmeraldGeneratorName())) {
            armorStands = new HashSet<>(gameManager.getArmorSande().keySet());
            resourceDisplayName = resourceSpawnConfig.getEmeraldName();
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
