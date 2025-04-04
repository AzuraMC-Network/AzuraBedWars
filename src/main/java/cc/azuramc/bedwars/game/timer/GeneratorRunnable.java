package cc.azuramc.bedwars.game.timer;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.event.Runnable;
import cc.azuramc.bedwars.utils.ArmorStandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源生成计时器
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 同时管理资源点上的盔甲架显示，包括资源名称、等级和下次刷新时间的显示。
 * </p>
 */
public class GeneratorRunnable {
    private final Game game;
    private boolean timer;
    private int taskId = -1;
    
    // 资源生成时间间隔（秒）
    private static final int IRON_SPAWN_INTERVAL = 2;
    private static final int GOLD_SPAWN_INTERVAL = 6;
    private static final int DIAMOND_SPAWN_INTERVAL = 30;
    private static final int EMERALD_SPAWN_INTERVAL = 55;
    
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
     * @param game 当前游戏实例
     */
    public GeneratorRunnable(Game game) {
        this.game = game;
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
                allArmor.addAll(game.getArmorSande().keySet());
                allArmor.addAll(game.getArmorStand().keySet());
            
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
     * 注册所有资源生成器
     */
    private void registerResourceGenerators() {
        // 铁锭生成器
        registerItemGenerator(IRON_GENERATOR_NAME, Material.IRON_INGOT, MapData.DropType.BASE, IRON_SPAWN_INTERVAL);
        
        // 金锭生成器
        registerItemGenerator(GOLD_GENERATOR_NAME, Material.GOLD_INGOT, MapData.DropType.BASE, GOLD_SPAWN_INTERVAL);
        
        // 钻石生成器
        registerItemGenerator(DIAMOND_GENERATOR_NAME, Material.DIAMOND, MapData.DropType.DIAMOND, DIAMOND_SPAWN_INTERVAL);
        
        // 绿宝石生成器
        registerItemGenerator(EMERALD_GENERATOR_NAME, Material.EMERALD, MapData.DropType.EMERALD, EMERALD_SPAWN_INTERVAL);
    }
    
    /**
     * 注册资源显示更新器
     */
    private void registerDisplayUpdaters() {
        // 钻石显示更新
        registerResourceDisplay(DIAMOND_TIME_DISPLAY, DIAMOND_GENERATOR_NAME, game.getArmorStand().keySet(), DIAMOND_NAME);
        
        // 绿宝石显示更新
        registerResourceDisplay(EMERALD_TIME_DISPLAY, EMERALD_GENERATOR_NAME, game.getArmorSande().keySet(), EMERALD_NAME);
    }
    
    /**
     * 注册资源生成器
     *
     * @param name 生成器名称
     * @param material 生成的物品类型
     * @param dropType 资源点类型
     * @param interval 生成间隔（秒）
     */
    private void registerItemGenerator(String name, Material material, MapData.DropType dropType, int interval) {
        game.getEventManager().registerRunnable(name, (seconds, currentEvent) -> 
            Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> 
                game.getMapData().getDropLocations(dropType)
                    .forEach(location -> 
                        dropItem(location, material)
                    )
            ), interval);
    }
    
    /**
     * 在指定位置生成物品
     *
     * @param location 生成位置
     * @param material 物品类型
     */
    private void dropItem(Location location, Material material) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().dropItem(
            location, 
            new ItemBuilderUtil()
                .setType(material)
                .setDisplayName(ITEM_DISPLAY_NAME)
                .getItem()
        ).setVelocity(ITEM_VELOCITY);
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
        game.getEventManager().registerRunnable(displayName, (seconds, currentEvent) -> 
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
            Runnable runnable = game.getEventManager().getRunnables().getOrDefault(generatorName, null);
            
            if (runnable != null) {
                timeRemaining = runnable.getSeconds() - runnable.getNextSeconds();
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
