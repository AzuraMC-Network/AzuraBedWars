package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.event.GameEvent;
import cc.azuramc.bedwars.compat.util.BedUtil;
import java.util.logging.Level;

/**
 * 床自毁事件
 * <p>
 * 在游戏进行到一定时间后，自动销毁所有队伍的床，
 * 使游戏进入到更激烈的阶段。该事件触发时，会向所有玩家播放末影龙咆哮音效
 * 并显示床自毁的全屏提示。
 * </p>
 */
public class BedDestroyedEvent extends GameEvent {
    // 事件相关常量
    private static final String EVENT_NAME = "床自毁";
    private static final int EXECUTE_SECONDS = 360; // 6分钟
    private static final int PRIORITY = 5;
    
    // 标题显示相关常量
    private static final String TITLE = "§c§l床自毁";
    private static final String SUBTITLE = "§e所有队伍床消失";
    private static final int TITLE_FADE_IN = 10;
    private static final int TITLE_STAY = 20;
    private static final int TITLE_FADE_OUT = 10;

    /**
     * 创建床自毁事件
     * 默认在游戏开始6分钟后触发，优先级为5
     */
    public BedDestroyedEvent() {
        super(EVENT_NAME, EXECUTE_SECONDS, PRIORITY);
    }

    /**
     * 执行床自毁事件
     * 销毁所有队伍的床，播放音效，并向玩家显示提示
     * 
     * @param gameManager 当前游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {
        if (gameManager == null) {
            AzuraBedWars.getInstance().getLogger().log(Level.WARNING, "无法执行床自毁事件：游戏实例为null");
            return;
        }

        // 在主线程销毁所有队伍的床
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            try {
                destroyAllBeds(gameManager);
            } catch (Exception e) {
                AzuraBedWars.getInstance().getLogger().log(Level.SEVERE, "床自毁事件执行异常", e);
            }
        });

        // 播放音效和显示标题
        SoundWrapper.broadcastEnderDragonGrowl(gameManager);
        gameManager.broadcastTitle(TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT, TITLE, SUBTITLE);
    }
    
    /**
     * 销毁所有队伍的床
     * 
     * @param gameManager 当前游戏实例
     */
    private void destroyAllBeds(GameManager gameManager) {
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            if (gameTeam == null) continue;
            if (gameTeam.isBedDestroy()) continue;

            BedUtil.destroyBed(gameTeam);
            gameTeam.setBedDestroy(true);
        }
    }
}
