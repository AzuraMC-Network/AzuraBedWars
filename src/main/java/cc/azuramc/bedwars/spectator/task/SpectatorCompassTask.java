package cc.azuramc.bedwars.spectator.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 指南针追踪运行器
 * <p>
 * 负责更新玩家手持指南针时的目标追踪，以及处理观战玩家的目标追踪和传送。
 * 该任务以1tick为周期持续运行，确保指南针指向信息和观战信息的及时更新。
 * </p>
 * @author an5w1r@163.com
 */
public class SpectatorCompassTask {
    /** 任务运行状态标志 */
    private boolean isRunning;
    
    /** 任务ID，用于在需要时取消任务 */
    private int taskId = -1;
    
    /** 更新频率（单位：tick） */
    private static final long UPDATE_FREQUENCY = 1L;
    
    /** 指南针材质 */
    private static final Material COMPASS_MATERIAL = Material.COMPASS;

    /**
     * 启动指南针追踪任务
     * <p>
     * 如果任务尚未启动，将创建一个循环任务来更新所有玩家的指南针指向
     * 和观战者的目标信息。该方法确保任务不会被重复启动。
     * </p>
     */
    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;

            taskId = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> {
                try {
                    updateAllPlayers();
                } catch (Exception e) {
                    // 记录错误但不停止任务运行
                    AzuraBedWars.getInstance().getLogger().warning("指南针追踪任务发生错误: " + e.getMessage());
                }
            }, 0L, UPDATE_FREQUENCY).getTaskId();
        }
    }
    
    /**
     * 停止指南针追踪任务
     * <p>
     * 如果任务正在运行，将取消该任务并重置运行状态。
     * </p>
     */
    public void stop() {
        if (this.isRunning && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            this.isRunning = false;
            this.taskId = -1;
        }
    }
    
    /**
     * 更新所有玩家的指南针和观战目标
     * <p>
     * 遍历所有在线玩家，为观战者更新目标信息，
     * 为手持指南针的玩家更新最近敌人信息。
     * </p>
     */
    private void updateAllPlayers() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer == null) {
                continue;
            }
            
            // 处理观战者
            if (gamePlayer.isSpectator()) {
                updateSpectator(gamePlayer);
                continue;
            }
            
            // 处理普通玩家的指南针
            updatePlayerCompass(gamePlayer);
        }
    }
    
    /**
     * 更新观战者的目标信息
     * @param spectator 观战玩家
     */
    private void updateSpectator(GamePlayer spectator) {
        SpectatorTarget target = spectator.getSpectatorTarget();
        if (target != null) {
            target.sendTip();
            target.autoTp();
        }
    }
    
    /**
     * 更新玩家手持指南针的目标信息
     * @param gamePlayer 游戏玩家
     */
    private void updatePlayerCompass(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        ItemStack heldItem = PlayerUtil.getItemInHand(player);
        if (isCompass(heldItem)) {
            gamePlayer.getPlayerCompass().sendClosestPlayer();
        }
    }
    
    /**
     * 检查物品是否为指南针
     * @param item 要检查的物品
     * @return 如果是指南针返回true，否则返回false
     */
    private boolean isCompass(ItemStack item) {
        return item != null && item.getType() == COMPASS_MATERIAL;
    }
    
    /**
     * 检查任务是否正在运行
     * @return 如果任务正在运行返回true，否则返回false
     */
    public boolean isRunning() {
        return isRunning;
    }
}
