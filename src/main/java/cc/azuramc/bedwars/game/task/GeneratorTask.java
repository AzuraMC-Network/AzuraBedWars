package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.TaskConfig;
import cc.azuramc.bedwars.event.GameEventRunnable;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.util.ArmorStandUtil;
import cc.azuramc.bedwars.util.ChatColorUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 资源生成计时器
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 同时管理资源点上的盔甲架显示，包括资源名称、等级和下次刷新时间的显示。
 * </p>
 * @author an5w1r@163.com
 */
public class GeneratorTask {
    private final GameManager gameManager;
    private boolean timer;
    private int taskId = -1;

    private final int IRON_SPAWN_INTERVAL;
    private final int GOLD_SPAWN_INTERVAL;
    private final int DIAMOND_SPAWN_INTERVAL;
    private final int EMERALD_SPAWN_INTERVAL;

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

    /**
     * 检测资源周围范围（方块）
     */
    private final double RESOURCE_CHECK_RADIUS;

    private final float NAME_DISPLAY_HEIGHT;
    private final float RESOURCE_TYPE_HEIGHT;
    private final float LEVEL_DISPLAY_HEIGHT;

    private final String IRON_GENERATOR_NAME;
    private final String GOLD_GENERATOR_NAME;
    private final String DIAMOND_GENERATOR_NAME;
    private final String DIAMOND_TIME_DISPLAY;
    private final String EMERALD_GENERATOR_NAME;
    private final String EMERALD_TIME_DISPLAY;

    private final String TIME_REMAINING_FORMAT;
    private final String DIAMOND_NAME;
    private final String EMERALD_NAME;
    private final String LEVEL_I;
    private final String LEVEL_II;
    private final String LEVEL_III;

    private final String ITEM_DISPLAY_NAME;
    private final Vector ITEM_VELOCITY;

    /**
     * 创建资源生成计时器
     *
     * @param gameManager 当前游戏实例
     */
    public GeneratorTask(GameManager gameManager) {
        this.gameManager = gameManager;
        TaskConfig.GeneratorConfig config = AzuraBedWars.getInstance().getTaskConfig().getGenerator();
        MessageConfig.Generator messageConfig = AzuraBedWars.getInstance().getMessageConfig().getGenerator();

        // 初始化所有配置值
        IRON_SPAWN_INTERVAL = config.getIronSpawnInterval();
        GOLD_SPAWN_INTERVAL = config.getGoldSpawnInterval();
        DIAMOND_SPAWN_INTERVAL = config.getDiamondSpawnInterval();
        EMERALD_SPAWN_INTERVAL = config.getEmeraldSpawnInterval();

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

        RESOURCE_CHECK_RADIUS = config.getResourceCheckRadius();

        NAME_DISPLAY_HEIGHT = config.getNameDisplayHeight();
        RESOURCE_TYPE_HEIGHT = config.getResourceTypeHeight();
        LEVEL_DISPLAY_HEIGHT = config.getLevelDisplayHeight();

        IRON_GENERATOR_NAME = messageConfig.getIronGeneratorName();
        GOLD_GENERATOR_NAME = messageConfig.getGoldGeneratorName();
        DIAMOND_GENERATOR_NAME = messageConfig.getDiamondGeneratorName();
        DIAMOND_TIME_DISPLAY = messageConfig.getDiamondTimeDisplay();
        EMERALD_GENERATOR_NAME = messageConfig.getEmeraldGeneratorName();
        EMERALD_TIME_DISPLAY = messageConfig.getEmeraldTimeDisplay();

        TIME_REMAINING_FORMAT = messageConfig.getTimeRemainingFormat();
        DIAMOND_NAME = messageConfig.getDiamondName();
        EMERALD_NAME = messageConfig.getEmeraldName();
        LEVEL_I = messageConfig.getLevelI();
        LEVEL_II = messageConfig.getLevelII();
        LEVEL_III = messageConfig.getLevelIII();

        ITEM_DISPLAY_NAME = ChatColorUtil.color("&a&a&a&a&a&a");
        ITEM_VELOCITY = new Vector(config.getItemVelocityX(), config.getItemVelocityY(), config.getItemVelocityZ());
    }

    /**
     * 启动资源生成计时器
     * 注册各类资源的生成事件和显示更新
     */
    public void start() {
        if (!this.timer) {
            timer = true;

            // 启动盔甲架位置更新任务
            startArmorStandUpdateTask();
            
            // 注册资源生成事件
            registerResourceGenerators();
            
            // 注册资源显示更新事件
            registerDisplayUpdaters();
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
        taskId = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> {
            try {
                List<ArmorStand> allArmor = new ArrayList<>();
                
                // 安全地添加盔甲架到列表中
                if (gameManager.getArmorSande() != null) {
                    Set<ArmorStand> armorSandeSet = gameManager.getArmorSande().keySet();
                    allArmor.addAll(armorSandeSet);
                }
                
                if (gameManager.getArmorStand() != null) {
                    Set<ArmorStand> armorStandSet = gameManager.getArmorStand().keySet();
                    allArmor.addAll(armorStandSet);
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
                        Bukkit.getLogger().warning("处理盔甲架时出错: " + e.getMessage());
                        iterator.remove(); // 移除有问题的盔甲架
                    }
                }
            } catch (Exception e) {
                // 捕获总体异常，确保任务不会终止
                Bukkit.getLogger().warning("盔甲架更新任务出错: " + e.getMessage());
            }
        }, 0L, 1L).getTaskId();
    }
    
    /**
     * 根据当前事件等级获取铁锭最大堆叠数量
     * 
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    private int getMaxIronStack(int eventLevel) {
        if (eventLevel <= 1) {
            return MAX_IRON_STACK_LEVEL_1;
        } else if (eventLevel == 2) {
            return MAX_IRON_STACK_LEVEL_2;
        } else {
            return MAX_IRON_STACK_LEVEL_3;
        }
    }
    
    /**
     * 根据当前事件等级获取金锭最大堆叠数量
     * 
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    private int getMaxGoldStack(int eventLevel) {
        if (eventLevel <= 1) {
            return MAX_GOLD_STACK_LEVEL_1;
        } else if (eventLevel == 2) {
            return MAX_GOLD_STACK_LEVEL_2;
        } else {
            return MAX_GOLD_STACK_LEVEL_3;
        }
    }
    
    /**
     * 根据当前事件等级获取钻石最大堆叠数量
     * 
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    private int getMaxDiamondStack(int eventLevel) {
        if (eventLevel <= 1) {
            return MAX_DIAMOND_STACK_LEVEL_1;
        } else if (eventLevel == 2) {
            return MAX_DIAMOND_STACK_LEVEL_2;
        } else {
            return MAX_DIAMOND_STACK_LEVEL_3;
        }
    }
    
    /**
     * 根据当前事件等级获取绿宝石最大堆叠数量
     * 
     * @param eventLevel 当前事件等级
     * @return 最大堆叠数量
     */
    private int getMaxEmeraldStack(int eventLevel) {
        if (eventLevel <= 1) {
            return MAX_EMERALD_STACK_LEVEL_1;
        } else if (eventLevel == 2) {
            return MAX_EMERALD_STACK_LEVEL_2;
        } else {
            return MAX_EMERALD_STACK_LEVEL_3;
        }
    }
    
    /**
     * 注册所有资源生成器
     */
    private void registerResourceGenerators() {
        // 铁锭生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(IRON_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.BASE)
                    .forEach(location -> 
                        dropItem(location, XMaterial.IRON_INGOT.get(), getMaxIronStack(currentEvent))
                    )
            ), IRON_SPAWN_INTERVAL);
        
        // 金锭生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(GOLD_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.BASE)
                    .forEach(location -> 
                        dropItem(location, XMaterial.GOLD_INGOT.get(), getMaxGoldStack(currentEvent))
                    )
            ), GOLD_SPAWN_INTERVAL);
        
        // 钻石生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(DIAMOND_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.DIAMOND)
                    .forEach(location -> 
                        dropItem(location, XMaterial.DIAMOND.get(), getMaxDiamondStack(currentEvent))
                    )
            ), DIAMOND_SPAWN_INTERVAL);
        
        // 绿宝石生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(EMERALD_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.EMERALD)
                    .forEach(location -> 
                        dropItem(location, XMaterial.EMERALD.get(), getMaxEmeraldStack(currentEvent))
                    )
            ), EMERALD_SPAWN_INTERVAL);
    }
    
    /**
     * 注册资源显示更新器
     */
    private void registerDisplayUpdaters() {
        try {
            // 钻石显示更新
            if (gameManager.getArmorStand() != null && !gameManager.getArmorStand().isEmpty()) {
                registerResourceDisplay(DIAMOND_TIME_DISPLAY, DIAMOND_GENERATOR_NAME, 
                    gameManager.getArmorStand().keySet(), DIAMOND_NAME);
            } else {
                Bukkit.getLogger().warning("无法注册钻石显示更新：盔甲架集合为空");
            }
            
            // 绿宝石显示更新
            if (gameManager.getArmorSande() != null && !gameManager.getArmorSande().isEmpty()) {
                registerResourceDisplay(EMERALD_TIME_DISPLAY, EMERALD_GENERATOR_NAME, 
                    gameManager.getArmorSande().keySet(), EMERALD_NAME);
            } else {
                Bukkit.getLogger().warning("无法注册绿宝石显示更新：盔甲架集合为空");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("注册资源显示更新器时出错: " + e.getMessage());
        }
    }
    
    /**
     * 在指定位置生成物品，但不超过最大堆叠数量
     *
     * @param location 生成位置
     * @param material 物品类型
     * @param maxStack 最大堆叠数量
     */
    private void dropItem(Location location, Material material, int maxStack) {
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
                .setDisplayName(ITEM_DISPLAY_NAME)
                .getItem()
        ).setVelocity(ITEM_VELOCITY);
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
            RESOURCE_CHECK_RADIUS, 
            RESOURCE_CHECK_RADIUS, 
            RESOURCE_CHECK_RADIUS
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
    
    /**
     * 注册资源显示更新器
     *
     * @param displayName 显示更新器名称
     * @param generatorName 对应的生成器名称
     * @param armorStands 盔甲架集合
     * @param resourceName 资源名称
     */
    private void registerResourceDisplay(String displayName, String generatorName, 
                                       java.util.Set<ArmorStand> armorStands, String resourceName) {
        if (armorStands == null || armorStands.isEmpty()) {
            Bukkit.getLogger().warning("尝试注册资源显示，但盔甲架集合为空: " + displayName);
            return;
        }
        
        // 创建盔甲架的安全副本，避免并发修改异常
        final Set<ArmorStand> safeArmorStands = new HashSet<>(armorStands);
        
        gameManager.getGameEventManager().registerRunnable(displayName, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                try {
                    Iterator<ArmorStand> iterator = safeArmorStands.iterator();
                    while (iterator.hasNext()) {
                        ArmorStand armorStand = iterator.next();
                        if (armorStand == null || !armorStand.isValid()) {
                            iterator.remove();
                            continue;
                        }
                        
                        try {
                            // 确保区块已加载
                            Location location = armorStand.getLocation();
                            if (location.getWorld() == null) {
                                iterator.remove();
                                continue;
                            }
                            
                            if (!location.getChunk().isLoaded()) {
                                location.getChunk().load();
                            }
                            
                            // 更新倒计时显示
                            updateTimeDisplay(armorStand, generatorName);
                            
                            // 更新资源名称显示
                            if (armorStand.getFallDistance() == RESOURCE_TYPE_HEIGHT) {
                                armorStand.setCustomName(resourceName);
                            }
                            
                            // 更新等级显示
                            if (armorStand.getFallDistance() == LEVEL_DISPLAY_HEIGHT) {
                                updateLevelDisplay(armorStand, currentEvent);
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("更新盔甲架显示时出错: " + e.getMessage());
                            // 移除问题盔甲架
                            iterator.remove();
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("处理资源显示更新时出错: " + e.getMessage());
                }
            })
        );
    }
    
    /**
     * 更新倒计时显示
     *
     * @param armorStand 盔甲架
     * @param generatorName 生成器名称
     */
    private void updateTimeDisplay(ArmorStand armorStand, String generatorName) {
        try {
            if (armorStand == null || !armorStand.isValid()) {
                return;
            }
            
            if (armorStand.getFallDistance() == NAME_DISPLAY_HEIGHT) {
                int timeRemaining = 0;
                GameEventRunnable gameEventRunnable = gameManager.getGameEventManager().getRunnable().getOrDefault(generatorName, null);
                
                if (gameEventRunnable != null) {
                    timeRemaining = gameEventRunnable.getSeconds() - gameEventRunnable.getNextSeconds();
                }
                
                String displayText = String.format(TIME_REMAINING_FORMAT, timeRemaining);
                armorStand.setCustomName(displayText);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("更新时间显示时出错: " + e.getMessage());
        }
    }
    
    /**
     * 更新等级显示
     *
     * @param armorStand 盔甲架
     * @param currentEvent 当前事件等级
     */
    private void updateLevelDisplay(ArmorStand armorStand, int currentEvent) {
        try {
            if (armorStand == null || !armorStand.isValid()) {
                return;
            }
            
            String levelDisplay;
            if (currentEvent <= 1) {
                levelDisplay = LEVEL_I;
            } else if (currentEvent == 2) {
                levelDisplay = LEVEL_II;
            } else {
                levelDisplay = LEVEL_III;
            }
            
            armorStand.setCustomName(levelDisplay);
        } catch (Exception e) {
            Bukkit.getLogger().warning("更新等级显示时出错: " + e.getMessage());
        }
    }
}
