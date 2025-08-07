package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.BedUtil;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import com.cryptomorin.xseries.XSound;

import java.util.logging.Level;

/**
 * 床自毁事件
 * <p>
 * 在游戏进行到一定时间后，自动销毁所有队伍的床，
 * 使游戏进入到更激烈的阶段。该事件触发时，会向所有玩家播放末影龙咆哮音效
 * 并显示床自毁的全屏提示。
 * </p>
 *
 * @author an5w1r@163.com
 */
public class BedDestroyedEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventSettingsConfig.BedDestroyedEvent bedDestroyedConfig = PLUGIN.getEventSettingsConfig().getBedDestroyedEvent();

    /**
     * 创建床自毁事件
     * 默认在游戏开始6分钟后触发，优先级为5
     */
    public BedDestroyedEvent() {
        super("床自毁", 360, 5);
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
        gameManager.broadcastTitleToAll(bedDestroyedConfig.getTitle(), bedDestroyedConfig.getSubtitle(),
                bedDestroyedConfig.getFadeIn(), bedDestroyedConfig.getTitleStay(), bedDestroyedConfig.getFadeOut());
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
