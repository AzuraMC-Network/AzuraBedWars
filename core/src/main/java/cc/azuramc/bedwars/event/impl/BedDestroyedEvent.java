package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.compat.util.BedUtil;
import cc.azuramc.bedwars.config.object.EventConfig;
import com.cryptomorin.xseries.XSound;

import java.util.logging.Level;

/**
 * 床自毁事件
 * <p>
 * 在游戏进行到一定时间后，自动销毁所有队伍的床，
 * 使游戏进入到更激烈的阶段。该事件触发时，会向所有玩家播放末影龙咆哮音效
 * 并显示床自毁的全屏提示。
 * </p>
 * @author an5w1r@163.com
 */
public class BedDestroyedEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventConfig.DestroyBedEvent CONFIG = PLUGIN.getEventConfig().getDestroyBedEvent();
    private static final MessageConfig.DestroyBed MESSAGE_CONFIG = PLUGIN.getMessageConfig().getDestroyBed();

    /**
     * 事件相关常量
     */
    private static final String EVENT_NAME = MESSAGE_CONFIG.getEventName();
    private static final int EXECUTE_SECONDS = CONFIG.getExecuteSecond();
    private static final int PRIORITY = 5;

    /**
     * 标题显示相关常量
     */
    private static final String TITLE = MESSAGE_CONFIG.getTitle().getTitleString();
    private static final String SUBTITLE = MESSAGE_CONFIG.getTitle().getSubtitle();
    private static final int TITLE_FADE_IN = CONFIG.getTitle().getFadeIn();
    private static final int TITLE_STAY = CONFIG.getTitle().getTitleStay();
    private static final int TITLE_FADE_OUT = CONFIG.getTitle().getFadeOut();

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
        gameManager.broadcastSound(XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 1, 1);
        gameManager.broadcastTitleToAll(TITLE, SUBTITLE, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
    }
    
    /**
     * 销毁所有队伍的床
     * 
     * @param gameManager 当前游戏实例
     */
    private void destroyAllBeds(GameManager gameManager) {
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            if (gameTeam == null) {
                continue;
            }
            if (gameTeam.isDestroyed()) {
                continue;
            }

            BedUtil.destroyBed(gameTeam);
            gameTeam.setDestroyed(true);
        }
    }
}
