package cc.azuramc.bedwars.game.task.generator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
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
 *
 * @author an5w1r@163.com
 */
public class GeneratorManager {

    /** Must match the fall distances that LoadGameUtil gives each hologram line */
    private static final float TIME_LINE = 6.0F;
    private static final float NAME_LINE = 5.0F;
    private static final float LEVEL_LINE = 4.0F;

    private final GameManager gameManager;
    private final Map<String, PublicResourceGenerator> publicResourceGeneratorMap;
    private final Map<String, PrivateResourceGenerator> privateResourceGeneratorMap;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.publicResourceGeneratorMap = new HashMap<>();
        this.privateResourceGeneratorMap = new HashMap<>();
    }

    public void initGeneratorTasks() {
        GeneratorConfig config = config();
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            PrivateResourceGenerator iron = null;
            if (XMaterial.IRON_INGOT.get() != null) {
                iron = new PrivateResourceGenerator(
                        gameManager,
                        "铁锭" + gameTeam.getName(),
                        gameTeam.getResourceDropLocation(),
                        XMaterial.IRON_INGOT.get(),
                        config.getIronMaxStack()
                );
            }
            addPrivateResourceTask(iron, ticks(config.getIronInterval()));

            PrivateResourceGenerator gold = null;
            if (XMaterial.GOLD_INGOT.get() != null) {
                gold = new PrivateResourceGenerator(
                        gameManager,
                        "金锭" + gameTeam.getName(),
                        gameTeam.getResourceDropLocation(),
                        XMaterial.GOLD_INGOT.get(),
                        config.getGoldMaxStack()
                );
            }
            addPrivateResourceTask(gold, ticks(config.getGoldInterval()));
        }

        PublicResourceGenerator diamond = null;
        if (XMaterial.DIAMOND.get() != null) {
            diamond = new PublicResourceGenerator(
                    gameManager,
                    "钻石",
                    XMaterial.DIAMOND.get(),
                    MapData.DropType.DIAMOND,
                    config.getMaxDiamondStackLevel1()
            );
        }
        addPublicResourceTask(diamond, ticks(config.getDiamondInterval()));

        PublicResourceGenerator emerald = null;
        if (XMaterial.EMERALD.get() != null) {
            emerald = new PublicResourceGenerator(
                    gameManager,
                    "绿宝石",
                    XMaterial.EMERALD.get(),
                    MapData.DropType.EMERALD,
                    config.getMaxEmeraldStackLevel1()
            );
        }
        addPublicResourceTask(emerald, ticks(config.getEmeraldInterval()));
    }

    private static long ticks(double seconds) {
        return Math.max(1L, Math.round(seconds * 20));
    }

    private static GeneratorConfig config() {
        return AzuraBedWars.getInstance().getGeneratorConfig();
    }

    /**
     * 添加私有资源任务，指定执行间隔
     *
     * @param task     任务实例
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
     * @param task     任务实例
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
        GeneratorConfig config = config();
        switch (resource) {
            case "钻石":
                if (level == 1) return config.getMaxDiamondStackLevel1();
                if (level == 2) return config.getMaxDiamondStackLevel2();
                if (level == 3) return config.getMaxDiamondStackLevel3();
                break;
            case "绿宝石":
                if (level == 1) return config.getMaxEmeraldStackLevel1();
                if (level == 2) return config.getMaxEmeraldStackLevel2();
                if (level == 3) return config.getMaxEmeraldStackLevel3();
                break;
        }
        return 0;
    }

    public void initDisplayUpdaters() {
        initResourceDisplayUpdater("钻石", gameManager.getArmorStand());
        initResourceDisplayUpdater("绿宝石", gameManager.getArmorSande());
    }

    private void updateResourceDisplay(Set<ArmorStand> armorStands, PublicResourceGenerator generator) {
        GeneratorConfig config = config();
        String resourceDisplayName = "钻石".equals(generator.getTaskName()) ? config.getDiamondName() : config.getEmeraldName();
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
            if (armorStand.getFallDistance() == TIME_LINE) {
                int timeRemaining = generator.getSecondsToNextDrop();
                String displayText = String.format(config.getTimeRemainingFormat(), timeRemaining);
                armorStand.setCustomName(displayText);
            }
            if (armorStand.getFallDistance() == NAME_LINE) {
                armorStand.setCustomName(resourceDisplayName);
            }
            if (armorStand.getFallDistance() == LEVEL_LINE) {
                int level = generator.getLevel();
                String levelDisplay = level <= 1 ? config.getLevelI() : (level == 2 ? config.getLevelII() : config.getLevelIII());
                armorStand.setCustomName(levelDisplay);
            }
        }
    }

    public void immediateUpdateDisplay(String generatorName) {
        PublicResourceGenerator generator = getPublicResourceGenerator(generatorName);
        if (generator == null) return;
        Set<ArmorStand> armorStands = "钻石".equals(generatorName) ? gameManager.getArmorStand() : gameManager.getArmorSande();
        updateResourceDisplay(new HashSet<>(armorStands), generator);
    }

    private void initResourceDisplayUpdater(String resourceName, Set<ArmorStand> armorStands) {
        if (armorStands == null || armorStands.isEmpty()) {
            LoggerUtil.warn("尝试初始化资源显示更新，但盔甲架集合为空: " + resourceName);
            return;
        }
        final Set<ArmorStand> safeArmorStands = new HashSet<>(armorStands);
        final PublicResourceGenerator generator = getPublicResourceGenerator(resourceName);
        if (generator == null) {
            LoggerUtil.warn("无法找到生成器: " + resourceName);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance().getPlugin(), () -> updateResourceDisplay(safeArmorStands, generator), 0L, 20L);
    }
}
