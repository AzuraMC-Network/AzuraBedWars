package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.event.GameEventRunnable;

/**
 * 绿宝石资源点升级事件
 * 随着游戏进行，绿宝石生成速度会加快
 */
public class EmeraldUpdateEventAbstract extends AbstractGameEvent {

    private static final AzuraBedWars plugin = AzuraBedWars.getInstance();

    // 绿宝石资源点刷新标识符
    private static final String EVENT_NAME = plugin.getMessageConfig().getEmeraldUpdateEvent().getEventName();
    
    // 定义各等级的绿宝石刷新时间（秒）
    private static final int LEVEL_2_REFRESH_SECONDS = plugin.getEventConfig().getEmeraldUpdateEvent().getLevel2RefreshSecond();
    private static final int LEVEL_3_REFRESH_SECONDS = plugin.getEventConfig().getEmeraldUpdateEvent().getLevel3RefreshSecond();
    
    // 当前升级等级
    private final int level;

    /**
     * 创建绿宝石升级事件
     *
     * @param level 升级目标等级
     * @param second 事件触发时间（秒）
     * @param priority 事件优先级
     */
    public EmeraldUpdateEventAbstract(int level, int second, int priority) {
        super("绿宝石升级到" + level + "级", second, priority);
        this.level = level;
    }

    /**
     * 执行绿宝石升级事件
     * 根据等级调整绿宝石刷新速度
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {
        // 获取绿宝石刷新任务
        GameEventRunnable gameEventRunnable = gameManager.getGameEventManager().getRunnable().get(EVENT_NAME);
        if (gameEventRunnable == null) {
            return; // 防止NPE
        }

        // 根据等级设置新的刷新时间
        int newRefreshSeconds = getRefreshSecondsForLevel();
        
        // 调整当前周期和未来周期的刷新时间
        updateRefreshTime(gameEventRunnable, newRefreshSeconds);
    }
    
    /**
     * 获取指定等级的刷新时间
     * 
     * @return 刷新间隔（秒）
     */
    private int getRefreshSecondsForLevel() {
        return switch (level) {
            case 2 -> LEVEL_2_REFRESH_SECONDS;
            case 3 -> LEVEL_3_REFRESH_SECONDS;
            default -> LEVEL_2_REFRESH_SECONDS; // 默认使用2级刷新时间
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
