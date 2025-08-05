package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ResourceSpawnConfig;
import cc.azuramc.bedwars.event.GameEventRunnable;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.packet.ArmorStandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.*;

/**
 * 资源生成计时器
 * <p>
 * 负责管理游戏中的资源生成，包括基地资源（铁锭、金锭）和地图资源（钻石、绿宝石）。
 * 同时管理资源点上的盔甲架显示，包括资源名称、等级和下次刷新时间的显示。
 * </p>
 * @author an5w1r@163.com
 */
public class GeneratorTask {
    private static final ResourceSpawnConfig resourceSpawnConfig = AzuraBedWars.getInstance().getResourceSpawnConfig();
    private final GameManager gameManager;
    private boolean timer;
    private int taskId = -1;

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
            startArmorStandUpdateTask();
            gameManager.getGeneratorManager().initGeneratorTasks();
            gameManager.getGeneratorManager().initDisplayUpdaters();
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
                        LoggerUtil.warn("处理盔甲架时出错: " + e.getMessage());
                        iterator.remove(); // 移除有问题的盔甲架
                    }
                }
            } catch (Exception e) {
                // 捕获总体异常，确保任务不会终止
                LoggerUtil.warn("盔甲架更新任务出错: " + e.getMessage());
            }
        }, 0L, 1L).getTaskId();
    }

    /**
     * 注册所有资源生成器
     */
    private void registerResourceGenerators() {
        gameManager.getGeneratorManager().initGeneratorTasks();
    }

    /**
     * 注册资源显示更新器
     */
    private void registerDisplayUpdaters() {
        try {
            // 钻石显示更新
            if (gameManager.getArmorStand() != null && !gameManager.getArmorStand().isEmpty()) {
                registerResourceDisplay(resourceSpawnConfig.getDiamondTimeDisplay(), resourceSpawnConfig.getDiamondGeneratorName(),
                        gameManager.getArmorStand().keySet(), resourceSpawnConfig.getDiamondName());
            } else {
                LoggerUtil.warn("无法注册钻石显示更新：盔甲架集合为空");
            }

            // 绿宝石显示更新
            if (gameManager.getArmorSande() != null && !gameManager.getArmorSande().isEmpty()) {
                registerResourceDisplay(resourceSpawnConfig.getEmeraldTimeDisplay(), resourceSpawnConfig.getEmeraldGeneratorName(),
                        gameManager.getArmorSande().keySet(), resourceSpawnConfig.getEmeraldName());
            } else {
                LoggerUtil.warn("无法注册绿宝石显示更新：盔甲架集合为空");
            }
        } catch (Exception e) {
            LoggerUtil.warn("注册资源显示更新器时出错: " + e.getMessage());
        }
    }

    /**
     * 注册资源显示更新器
     *
     * @param displayName 显示更新器名称
     * @param generatorName 对应的生成器名称
     * @param armorStands 盔甲架集合
     * @param resourceName 资源名称
     */
    private void registerResourceDisplay(String displayName, String generatorName, Set<ArmorStand> armorStands, String resourceName) {
        if (armorStands == null || armorStands.isEmpty()) {
            LoggerUtil.warn("尝试注册资源显示，但盔甲架集合为空: " + displayName);
            return;
        }

        // 创建盔甲架的安全副本，避免并发修改异常
        final Set<ArmorStand> safeArmorStands = new HashSet<>(armorStands);

        gameManager.getGameEventManager().registerRunnable(displayName, (seconds, currentEventLevel) ->
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
                            if (armorStand.getFallDistance() == resourceSpawnConfig.getResourceTypeHeight()) {
                                armorStand.setCustomName(resourceName);
                            }

                            // 更新等级显示
                            if (armorStand.getFallDistance() == resourceSpawnConfig.getLevelDisplayHeight()) {
                                updateLevelDisplay(armorStand, currentEventLevel);
                            }
                        } catch (Exception e) {
                            LoggerUtil.warn("更新盔甲架显示时出错: " + e.getMessage());
                            // 移除问题盔甲架
                            iterator.remove();
                        }
                    }
                } catch (Exception e) {
                    LoggerUtil.warn("处理资源显示更新时出错: " + e.getMessage());
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

            if (armorStand.getFallDistance() == resourceSpawnConfig.getNameDisplayHeight()) {
                int timeRemaining = 0;
                GameEventRunnable gameEventRunnable = gameManager.getGameEventManager().getRunnable().getOrDefault(generatorName, null);

                if (gameEventRunnable != null) {
                    timeRemaining = gameEventRunnable.getSeconds() - gameEventRunnable.getNextSeconds();
                }

                String displayText = String.format(resourceSpawnConfig.getTimeRemainingFormat(), timeRemaining);
                armorStand.setCustomName(displayText);
            }
        } catch (Exception e) {
            LoggerUtil.warn("更新时间显示时出错: " + e.getMessage());
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
                levelDisplay = resourceSpawnConfig.getLevelI();
            } else if (currentEvent == 2) {
                levelDisplay = resourceSpawnConfig.getLevelII();
            } else {
                levelDisplay = resourceSpawnConfig.getLevelIII();
            }

            armorStand.setCustomName(levelDisplay);
        } catch (Exception e) {
            LoggerUtil.warn("更新等级显示时出错: " + e.getMessage());
        }
    }
}
