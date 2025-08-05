package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.event.GameEventRunnable;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.task.generator.PublicResourceGenerator;

/**
 * 钻石资源点升级事件
 * 随着游戏进行，钻石生成速度会加快
 *
 * @author an5w1r@163.com
 */
public class DiamondUpdateEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventSettingsConfig.DiamondUpdateEvent diamondUpdateConfig = PLUGIN.getEventSettingsConfig().getDiamondUpdateEvent();

    /**
     * 当前升级等级
     */
    private final int level;

    /**
     * 创建钻石升级事件
     *
     * @param level 升级目标等级
     * @param second 事件触发时间（秒）
     * @param priority 事件优先级
     */
    public DiamondUpdateEvent(int level, int second, int priority) {
        super("钻石资源点升级到" + toRoman(level) + "级", second, priority);
        this.level = level;
    }

    /**
     * 执行钻石升级事件
     * 根据等级调整钻石刷新速度
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {
        GeneratorManager genManager = gameManager.getGeneratorManager();
        PublicResourceGenerator diamondGen = genManager.getPublicResourceGenerator("钻石");
        if (diamondGen == null) {
            return;
        }
        int newRefreshSeconds = getRefreshSecondsForLevel();
        diamondGen.setInterval(newRefreshSeconds * 20L);
        diamondGen.setLevel(level);
        int newMaxStack = genManager.getMaxStackForResource("钻石", level);
        diamondGen.setMaxStack(newMaxStack);
        // 重新启动任务以应用新的间隔
        diamondGen.restartTask();
    }

    /**
     * 获取指定等级的刷新时间
     *
     * @return 刷新间隔（秒）
     */
    private int getRefreshSecondsForLevel() {
        return switch (level) {
            case 2 -> diamondUpdateConfig.getLevel2RefreshSecond();
            case 3 -> diamondUpdateConfig.getLevel3RefreshSecond();
            default -> diamondUpdateConfig.getLevel1RefreshSecond();
        };
    }

    /**
     * 更新资源刷新时间
     *
     * @param gameEventRunnable 刷新任务
     * @param newRefreshSeconds 新的刷新时间（秒）
     */
    private void updateRefreshTime(GameEventRunnable gameEventRunnable, int newRefreshSeconds) {
        // 如果当前倒计时比新设置的时间长，则直接调整为新时间
        if (gameEventRunnable.getNextSeconds() > newRefreshSeconds) {
            gameEventRunnable.setNextSeconds(newRefreshSeconds);
        }

        // 设置未来周期的刷新间隔
        gameEventRunnable.setSeconds(newRefreshSeconds);
    }
}
