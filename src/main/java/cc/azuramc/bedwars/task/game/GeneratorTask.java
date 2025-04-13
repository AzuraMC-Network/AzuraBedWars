package cc.azuramc.bedwars.task.game;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.arena.MapData;
import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.event.GameEventRunnable;
import cc.azuramc.bedwars.utils.entity.ArmorStandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 资源生成计时器
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 同时管理资源点上的盔甲架显示，包括资源名称、等级和下次刷新时间的显示。
 * </p>
 */
public class GeneratorTask {
    private final GameManager gameManager;
    private boolean timer;
    private int taskId = -1;
    
    // 资源生成时间间隔（秒）
    private static final int IRON_SPAWN_INTERVAL = 2;
    private static final int GOLD_SPAWN_INTERVAL = 6;
    private static final int DIAMOND_SPAWN_INTERVAL = 30;
    private static final int EMERALD_SPAWN_INTERVAL = 55;
    
    // 基础资源生成最大堆叠数量（一级）
    private static final int MAX_IRON_STACK_LEVEL_1 = 48;
    private static final int MAX_GOLD_STACK_LEVEL_1 = 8;
    private static final int MAX_DIAMOND_STACK_LEVEL_1 = 4;
    private static final int MAX_EMERALD_STACK_LEVEL_1 = 2;
    
    // 二级资源生成最大堆叠数量
    private static final int MAX_IRON_STACK_LEVEL_2 = 48;
    private static final int MAX_GOLD_STACK_LEVEL_2 = 8;
    private static final int MAX_DIAMOND_STACK_LEVEL_2 = 6;
    private static final int MAX_EMERALD_STACK_LEVEL_2 = 4;
    
    // 三级资源生成最大堆叠数量
    private static final int MAX_IRON_STACK_LEVEL_3 = 64;
    private static final int MAX_GOLD_STACK_LEVEL_3 = 12;
    private static final int MAX_DIAMOND_STACK_LEVEL_3 = 8;
    private static final int MAX_EMERALD_STACK_LEVEL_3 = 4;
    
    // 检测资源周围范围（方块）
    private static final double RESOURCE_CHECK_RADIUS = 3;
    
    // 盔甲架标识
    private static final float NAME_DISPLAY_HEIGHT = 6.0F;
    private static final float RESOURCE_TYPE_HEIGHT = 5.0F;
    private static final float LEVEL_DISPLAY_HEIGHT = 4.0F;
    
    // 资源名称
    private static final String IRON_GENERATOR_NAME = "铁刷新";
    private static final String GOLD_GENERATOR_NAME = "金刷新";
    private static final String DIAMOND_GENERATOR_NAME = "钻石刷新";
    private static final String DIAMOND_TIME_DISPLAY = "钻石时间显示";
    private static final String EMERALD_GENERATOR_NAME = "绿宝石刷新";
    private static final String EMERALD_TIME_DISPLAY = "绿宝石时间显示";
    
    // 显示文本
    private static final String TIME_REMAINING_FORMAT = "§e将在§c%d§e秒后刷新";
    private static final String DIAMOND_NAME = "§b钻石";
    private static final String EMERALD_NAME = "§2绿宝石";
    private static final String LEVEL_I = "§e等级 §cI";
    private static final String LEVEL_II = "§e等级 §cII";
    private static final String LEVEL_III = "§e等级 §cIII";
    
    // 物品属性
    private static final String ITEM_DISPLAY_NAME = "§a§a§a§a§a§a";
    private static final Vector ITEM_VELOCITY = new Vector(0.0D, 0.1D, 0.0D);

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
            List<ArmorStand> allArmor = new ArrayList<>();
            allArmor.addAll(gameManager.getArmorSande().keySet());
            allArmor.addAll(gameManager.getArmorStand().keySet());

            for (ArmorStand as : allArmor) {
                if (as == null) continue;

                Location loc = as.getLocation();
                if (!loc.getChunk().isLoaded()) {
                    loc.getChunk().load();
                }
                ArmorStandUtil.moveArmorStand(as, as.getLocation().getY());
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
                        dropItem(location, Material.IRON_INGOT, getMaxIronStack(currentEvent))
                    )
            ), IRON_SPAWN_INTERVAL);
        
        // 金锭生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(GOLD_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.BASE)
                    .forEach(location -> 
                        dropItem(location, Material.GOLD_INGOT, getMaxGoldStack(currentEvent))
                    )
            ), GOLD_SPAWN_INTERVAL);
        
        // 钻石生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(DIAMOND_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.DIAMOND)
                    .forEach(location -> 
                        dropItem(location, Material.DIAMOND, getMaxDiamondStack(currentEvent))
                    )
            ), DIAMOND_SPAWN_INTERVAL);
        
        // 绿宝石生成器（根据事件等级调整堆叠上限）
        gameManager.getGameEventManager().registerRunnable(EMERALD_GENERATOR_NAME, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                gameManager.getMapData().getDropLocations(MapData.DropType.EMERALD)
                    .forEach(location -> 
                        dropItem(location, Material.EMERALD, getMaxEmeraldStack(currentEvent))
                    )
            ), EMERALD_SPAWN_INTERVAL);
    }
    
    /**
     * 注册资源显示更新器
     */
    private void registerDisplayUpdaters() {
        // 钻石显示更新
        registerResourceDisplay(DIAMOND_TIME_DISPLAY, DIAMOND_GENERATOR_NAME, gameManager.getArmorStand().keySet(), DIAMOND_NAME);
        
        // 绿宝石显示更新
        registerResourceDisplay(EMERALD_TIME_DISPLAY, EMERALD_GENERATOR_NAME, gameManager.getArmorSande().keySet(), EMERALD_NAME);
    }
    
    /**
     * 在指定位置生成物品，但不超过最大堆叠数量
     *
     * @param location 生成位置
     * @param material 物品类型
     * @param maxStack 最大堆叠数量
     */
    private void dropItem(Location location, Material material, int maxStack) {
        if (location == null || location.getWorld() == null) return;
        
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
        if (location == null || location.getWorld() == null) return 0;
        
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
        gameManager.getGameEventManager().registerRunnable(displayName, (seconds, currentEvent) ->
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                for (ArmorStand armorStand : armorStands) {
                    if (armorStand == null) continue;
                    
                    // 确保区块已加载
                    if (!armorStand.getLocation().getChunk().isLoaded()) {
                        armorStand.getLocation().getChunk().load();
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
        if (armorStand.getFallDistance() == NAME_DISPLAY_HEIGHT) {
            int timeRemaining = 0;
            GameEventRunnable gameEventRunnable = gameManager.getGameEventManager().getRunnables().getOrDefault(generatorName, null);
            
            if (gameEventRunnable != null) {
                timeRemaining = gameEventRunnable.getSeconds() - gameEventRunnable.getNextSeconds();
            }
            
            armorStand.setCustomName(String.format(TIME_REMAINING_FORMAT, timeRemaining));
        }
    }
    
    /**
     * 更新等级显示
     *
     * @param armorStand 盔甲架
     * @param currentEvent 当前事件等级
     */
    private void updateLevelDisplay(ArmorStand armorStand, int currentEvent) {
        if (currentEvent <= 1) {
            armorStand.setCustomName(LEVEL_I);
        } else if (currentEvent == 2) {
            armorStand.setCustomName(LEVEL_II);
        } else {
            armorStand.setCustomName(LEVEL_III);
        }
    }
}
