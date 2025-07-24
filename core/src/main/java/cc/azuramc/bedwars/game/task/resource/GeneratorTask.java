package cc.azuramc.bedwars.game.task.resource;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.TaskConfig;
import cc.azuramc.bedwars.event.GameEventRunnable;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.util.ArmorStandUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 资源生成计时器 - 重构版本
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 提供灵活的API接口来动态调整资源生成等级和参数。
 * </p>
 * @author an5w1r@163.com
 */
public class GeneratorTask {

    private final GameManager gameManager;
    private volatile boolean running = false;
    private int armorStandTaskId = -1;

    // 资源配置映射
    private final Map<ResourceType, ResourceConfig> resourceConfigs = new ConcurrentHashMap<>();

    // 显示配置
    private final double RESOURCE_CHECK_RADIUS;
    private final float NAME_DISPLAY_HEIGHT;
    private final float RESOURCE_TYPE_HEIGHT;
    private final float LEVEL_DISPLAY_HEIGHT;
    private final Vector ITEM_VELOCITY;

    // 消息配置
    private final Map<ResourceType, String> generatorNames = new EnumMap<>(ResourceType.class);
    private final Map<ResourceType, String> resourceNames = new EnumMap<>(ResourceType.class);
    private final Map<ResourceType, String> timeDisplayNames = new EnumMap<>(ResourceType.class);
    private final String TIME_REMAINING_FORMAT;
    private final String LEVEL_I, LEVEL_II, LEVEL_III;

    // 事件监听器
    private final Set<Consumer<ResourceGenerationEvent>> generationListeners = ConcurrentHashMap.newKeySet();

    public GeneratorTask(GameManager gameManager) {
        this.gameManager = gameManager;
        TaskConfig.GeneratorConfig config = AzuraBedWars.getInstance().getTaskConfig().getGenerator();
        MessageConfig.Generator messageConfig = AzuraBedWars.getInstance().getMessageConfig().getGenerator();

        // 初始化资源配置
        initializeResourceConfigs(config);

        // 初始化显示配置
        RESOURCE_CHECK_RADIUS = config.getResourceCheckRadius();
        NAME_DISPLAY_HEIGHT = config.getNameDisplayHeight();
        RESOURCE_TYPE_HEIGHT = config.getResourceTypeHeight();
        LEVEL_DISPLAY_HEIGHT = config.getLevelDisplayHeight();
        ITEM_VELOCITY = new Vector(config.getItemVelocityX(), config.getItemVelocityY(), config.getItemVelocityZ());

        // 初始化消息配置
        initializeMessageConfigs(messageConfig);
        TIME_REMAINING_FORMAT = messageConfig.getTimeRemainingFormat();
        LEVEL_I = messageConfig.getLevelI();
        LEVEL_II = messageConfig.getLevelII();
        LEVEL_III = messageConfig.getLevelIII();
    }

    private void initializeResourceConfigs(TaskConfig.GeneratorConfig config) {
        resourceConfigs.put(ResourceType.IRON, new ResourceConfig(
                config.getIronSpawnInterval(),
                config.getMaxIronStackLevel1(),
                config.getMaxIronStackLevel2(),
                config.getMaxIronStackLevel3()
        ));

        resourceConfigs.put(ResourceType.GOLD, new ResourceConfig(
                config.getGoldSpawnInterval(),
                config.getMaxGoldStackLevel1(),
                config.getMaxGoldStackLevel2(),
                config.getMaxGoldStackLevel3()
        ));

        resourceConfigs.put(ResourceType.DIAMOND, new ResourceConfig(
                config.getDiamondSpawnInterval(),
                config.getMaxDiamondStackLevel1(),
                config.getMaxDiamondStackLevel2(),
                config.getMaxDiamondStackLevel3()
        ));

        resourceConfigs.put(ResourceType.EMERALD, new ResourceConfig(
                config.getEmeraldSpawnInterval(),
                config.getMaxEmeraldStackLevel1(),
                config.getMaxEmeraldStackLevel2(),
                config.getMaxEmeraldStackLevel3()
        ));
    }

    private void initializeMessageConfigs(MessageConfig.Generator messageConfig) {
        generatorNames.put(ResourceType.IRON, messageConfig.getIronGeneratorName());
        generatorNames.put(ResourceType.GOLD, messageConfig.getGoldGeneratorName());
        generatorNames.put(ResourceType.DIAMOND, messageConfig.getDiamondGeneratorName());
        generatorNames.put(ResourceType.EMERALD, messageConfig.getEmeraldGeneratorName());

        resourceNames.put(ResourceType.DIAMOND, messageConfig.getDiamondName());
        resourceNames.put(ResourceType.EMERALD, messageConfig.getEmeraldName());

        timeDisplayNames.put(ResourceType.DIAMOND, messageConfig.getDiamondTimeDisplay());
        timeDisplayNames.put(ResourceType.EMERALD, messageConfig.getEmeraldTimeDisplay());
    }


    /**
     * 启动资源生成计时器
     */
    public void start() {
        if (!running) {
            running = true;
            startArmorStandUpdateTask();
            registerAllResourceGenerators();
            registerAllDisplayUpdaters();
            LoggerUtil.debug("GeneratorTask$start | started all generator tasks");
        }
    }

    /**
     * 停止所有计时器任务
     */
    public void stop() {
        if (running) {
            running = false;
            if (armorStandTaskId != -1) {
                Bukkit.getScheduler().cancelTask(armorStandTaskId);
                armorStandTaskId = -1;
            }
            LoggerUtil.debug("GeneratorTask$stop | stopped all generator tasks");
        }
    }

    /**
     * 获取指定资源类型的配置
     */
    public ResourceConfig getResourceConfig(ResourceType resourceType) {
        return resourceConfigs.get(resourceType).copy();
    }

    /**
     * 更新指定资源类型的配置
     */
    public void updateResourceConfig(ResourceType resourceType, ResourceConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ResourceConfig cannot be null");
        }
        resourceConfigs.put(resourceType, config.copy());

        // 如果正在运行，重新注册生成器
        if (running) {
            reregisterResourceGenerator(resourceType);
        }

        LoggerUtil.debug("GeneratorTask$updateResourceConfig | updated resource config for " + resourceType + " -> " + config.getSpawnInterval() + " seconds interval");
    }

    /**
     * 设置指定资源在特定等级的最大堆叠数量
     */
    public void setMaxStack(ResourceType resourceType, int level, int maxStack) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        if (config != null) {
            config.setMaxStack(level, maxStack);
            LoggerUtil.debug("GeneratorTask$setMaxStack | set max stack for " + resourceType + " level " + level + " to " + maxStack);
        }
    }

    /**
     * 获取指定资源在特定等级的最大堆叠数量
     */
    public int getMaxStack(ResourceType resourceType, int level) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        return config != null ? config.getMaxStack(level) : 0;
    }

    /**
     * 设置指定资源的生成间隔
     */
    public void setSpawnInterval(ResourceType resourceType, int intervalSeconds) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        if (config != null) {
            config.setSpawnInterval(intervalSeconds);
            if (running) {
                reregisterResourceGenerator(resourceType);
            }
            LoggerUtil.debug("GeneratorTask$setSpawnInterval | set spawn interval for " + resourceType + " to " + intervalSeconds + " seconds");
        }
    }

    /**
     * 启用/禁用指定资源的生成
     */
    public void setResourceEnabled(ResourceType resourceType, boolean enabled) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        if (config != null) {
            config.setEnabled(enabled);
            if (running) {
                if (enabled) {
                    reregisterResourceGenerator(resourceType);
                } else {
                    unregisterResourceGenerator(resourceType);
                }
            }
            LoggerUtil.debug("GeneratorTask$setResourceEnabled | " + resourceType + " enabled: " + (enabled ? "enabled" : "disabled"));
        }
    }

    /**
     * 手动触发指定资源的生成
     */
    public void triggerResourceGeneration(ResourceType resourceType) {
        // 获取当前事件等级，默认为1
        int currentEventLevel = getCurrentEventLevel();
        triggerResourceGeneration(resourceType, currentEventLevel);
    }

    /**
     * 手动触发指定资源在特定等级的生成
     */
    public void triggerResourceGeneration(ResourceType resourceType, int eventLevel) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        if (config != null && config.isEnabled()) {
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                gameManager.getMapData().getDropLocations(resourceType.getDropType())
                        .forEach(location -> dropItem(location, resourceType.getMaterial(), config.getMaxStack(eventLevel)));
            });
        }
    }

    /**
     * 添加资源生成事件监听器
     */
    public void addGenerationListener(Consumer<ResourceGenerationEvent> listener) {
        generationListeners.add(listener);
    }

    /**
     * 移除资源生成事件监听器
     */
    public void removeGenerationListener(Consumer<ResourceGenerationEvent> listener) {
        generationListeners.remove(listener);
    }

    /**
     * 获取所有资源类型的当前配置
     */
    public Map<ResourceType, ResourceConfig> getAllResourceConfigs() {
        Map<ResourceType, ResourceConfig> result = new EnumMap<>(ResourceType.class);
        resourceConfigs.forEach((type, config) -> result.put(type, config.copy()));
        return result;
    }

    /**
     * 批量更新资源配置
     */
    public void updateResourceConfigs(Map<ResourceType, ResourceConfig> configs) {
        configs.forEach(this::updateResourceConfig);
    }

    private void startArmorStandUpdateTask() {
        armorStandTaskId = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> {
            if (!running) return;

            try {
                updateAllArmorStands();
            } catch (Exception e) {
                LoggerUtil.warn("盔甲架更新任务出错: " + e.getMessage());
            }
        }, 0L, 1L).getTaskId();
    }

    private void updateAllArmorStands() {
        List<ArmorStand> allArmor = new ArrayList<>();

        // 安全地收集所有盔甲架
        Optional.ofNullable(gameManager.getArmorSande())
                .map(Map::keySet)
                .ifPresent(allArmor::addAll);

        Optional.ofNullable(gameManager.getArmorStand())
                .map(Map::keySet)
                .ifPresent(allArmor::addAll);

        // 更新盔甲架位置
        allArmor.removeIf(as -> !updateSingleArmorStand(as));
    }

    private boolean updateSingleArmorStand(ArmorStand armorStand) {
        if (armorStand == null || !armorStand.isValid()) {
            return false;
        }

        try {
            Location loc = armorStand.getLocation();
            if (loc.getWorld() == null) return false;

            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            ArmorStandUtil.moveArmorStand(armorStand, loc.getY());
            return true;
        } catch (Exception e) {
            LoggerUtil.warn("更新单个盔甲架失败: " + e.getMessage());
            return false;
        }
    }

    private void registerAllResourceGenerators() {
        for (ResourceType resourceType : ResourceType.values()) {
            registerResourceGenerator(resourceType);
        }
    }

    private void registerResourceGenerator(ResourceType resourceType) {
        ResourceConfig config = resourceConfigs.get(resourceType);
        if (config == null || !config.isEnabled()) {
            return;
        }

        String generatorName = generatorNames.get(resourceType);
        if (generatorName == null) {
            LoggerUtil.warn("未找到资源生成器名称: " + resourceType);
            return;
        }

        gameManager.getGameEventManager().registerRunnable(generatorName, (seconds, currentEvent) ->
                Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                    int maxStack = config.getMaxStack(currentEvent);
                    gameManager.getMapData().getDropLocations(resourceType.getDropType())
                            .forEach(location -> {
                                dropItem(location, resourceType.getMaterial(), maxStack);
                                fireGenerationEvent(resourceType, location, 1, currentEvent);
                            });
                }), config.getSpawnInterval());
    }

    private void reregisterResourceGenerator(ResourceType resourceType) {
        unregisterResourceGenerator(resourceType);
        registerResourceGenerator(resourceType);
    }

    private void unregisterResourceGenerator(ResourceType resourceType) {
        String generatorName = generatorNames.get(resourceType);
        if (generatorName != null) {
            gameManager.getGameEventManager().getRunnable().remove(generatorName);
        }
    }

    /**
     * 获取当前事件等级
     * 根据GameEventManager的currentEvent()方法判断当前等级
     */
    private int getCurrentEventLevel() {
        try {
            // 根据当前事件的优先级判断等级
            int currentEventPriority = gameManager.getGameEventManager().currentEvent().getPriority();

            // 根据事件优先级推断等级
            if (currentEventPriority >= 3) { // DiamondLevel3 和 EmeraldLevel3 的优先级
                return 3;
            } else if (currentEventPriority >= 1) { // DiamondLevel2 和 EmeraldLevel2 的优先级
                return 2;
            } else {
                return 1; // 初始等级
            }
        } catch (Exception e) {
            LoggerUtil.warn("获取当前事件等级失败，使用默认等级1: " + e.getMessage());
            return 1;
        }
    }

    private void registerAllDisplayUpdaters() {
        registerDisplayUpdater(ResourceType.DIAMOND);
        registerDisplayUpdater(ResourceType.EMERALD);
    }

    private void registerDisplayUpdater(ResourceType resourceType) {
        String displayName = timeDisplayNames.get(resourceType);
        String generatorName = generatorNames.get(resourceType);
        String resourceName = resourceNames.get(resourceType);

        if (displayName == null || generatorName == null || resourceName == null) {
            LoggerUtil.warn("显示配置不完整，跳过注册: " + resourceType);
            return;
        }

        Set<ArmorStand> armorStands = getArmorStandsForResource(resourceType);
        if (armorStands == null || armorStands.isEmpty()) {
            LoggerUtil.warn("无盔甲架可用于显示更新: " + resourceType);
            return;
        }

        registerResourceDisplay(displayName, generatorName, new HashSet<>(armorStands), resourceName);
    }

    private Set<ArmorStand> getArmorStandsForResource(ResourceType resourceType) {
        return switch (resourceType) {
            case DIAMOND -> Optional.ofNullable(gameManager.getArmorStand()).map(Map::keySet).orElse(null);
            case EMERALD -> Optional.ofNullable(gameManager.getArmorSande()).map(Map::keySet).orElse(null);
            default -> null;
        };
    }

    private void dropItem(Location location, Material material, int maxStack) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        int currentAmount = countNearbyItems(location, material);
        if (currentAmount >= maxStack) {
            return;
        }

        location.getWorld().dropItem(
                location,
                new ItemBuilder()
                        .setType(material)
                        .getItem()
        ).setVelocity(ITEM_VELOCITY);
    }

    private int countNearbyItems(Location location, Material material) {
        if (location == null || location.getWorld() == null) {
            return 0;
        }

        return location.getWorld().getNearbyEntities(
                        location, RESOURCE_CHECK_RADIUS, RESOURCE_CHECK_RADIUS, RESOURCE_CHECK_RADIUS
                ).stream()
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .filter(item -> item.getItemStack().getType() == material)
                .mapToInt(item -> item.getItemStack().getAmount())
                .sum();
    }

    private void registerResourceDisplay(String displayName, String generatorName,
                                         Set<ArmorStand> armorStands, String resourceName) {
        gameManager.getGameEventManager().registerRunnable(displayName, (seconds, currentEvent) ->
                Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                    armorStands.removeIf(armorStand -> !updateArmorStandDisplay(armorStand, generatorName, resourceName, currentEvent));
                })
        );
    }

    private boolean updateArmorStandDisplay(ArmorStand armorStand, String generatorName, String resourceName, int currentEvent) {
        if (armorStand == null || !armorStand.isValid()) {
            return false;
        }

        try {
            Location location = armorStand.getLocation();
            if (location.getWorld() == null) return false;

            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }

            float fallDistance = armorStand.getFallDistance();

            if (fallDistance == NAME_DISPLAY_HEIGHT) {
                updateTimeDisplay(armorStand, generatorName);
            } else if (fallDistance == RESOURCE_TYPE_HEIGHT) {
                armorStand.setCustomName(resourceName);
            } else if (fallDistance == LEVEL_DISPLAY_HEIGHT) {
                updateLevelDisplay(armorStand, currentEvent);
            }

            return true;
        } catch (Exception e) {
            LoggerUtil.warn("更新盔甲架显示失败: " + e.getMessage());
            return false;
        }
    }

    private void updateTimeDisplay(ArmorStand armorStand, String generatorName) {
        GameEventRunnable gameEventRunnable = gameManager.getGameEventManager()
                .getRunnable().get(generatorName);

        int timeRemaining = 0;
        if (gameEventRunnable != null) {
            timeRemaining = gameEventRunnable.getSeconds() - gameEventRunnable.getNextSeconds();
        }

        String displayText = String.format(TIME_REMAINING_FORMAT, timeRemaining);
        armorStand.setCustomName(displayText);
    }

    private void updateLevelDisplay(ArmorStand armorStand, int currentEvent) {
        String levelDisplay = switch (currentEvent) {
            case 1 -> LEVEL_I;
            case 2 -> LEVEL_II;
            default -> LEVEL_III;
        };
        armorStand.setCustomName(levelDisplay);
    }

    private void fireGenerationEvent(ResourceType resourceType, Location location, int amount, int eventLevel) {
        if (!generationListeners.isEmpty()) {
            ResourceGenerationEvent event = new ResourceGenerationEvent(resourceType, location, amount, eventLevel);
            generationListeners.forEach(listener -> {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    LoggerUtil.warn("资源生成事件监听器执行失败: " + e.getMessage());
                }
            });
        }
    }
}