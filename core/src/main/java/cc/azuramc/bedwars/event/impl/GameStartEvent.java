package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.spectator.task.SpectatorCompassTask;
import cc.azuramc.bedwars.game.task.GeneratorTask;
import com.cryptomorin.xseries.XSound;

/**
 * 游戏开始事件
 * 负责处理游戏开始时的初始化
 *
 * @author an5w1r@163.com
 */
public class GameStartEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventConfig.StartEvent CONFIG = PLUGIN.getEventConfig().getStartEvent();
    private static final MessageConfig.Start MESSAGE_EVENT = PLUGIN.getMessageConfig().getStart();

    /**
     * 创建游戏开始事件
     */
    public GameStartEvent() {
        super(MESSAGE_EVENT.getEventName(), PLUGIN.getEventConfig().getStartEvent().getCountDown(), PLUGIN.getEventConfig().getStartEvent().getEventPriority());
    }

    /**
     * 处理游戏开始倒计时
     *
     * @param gameManager 游戏实例
     * @param seconds 剩余秒数
     */
    @Override
    public void executeRunnable(GameManager gameManager, int seconds) {
        gameManager.broadcastSound(XSound.UI_BUTTON_CLICK.get(), 1f, 1f);
        gameManager.broadcastTitleToAll(
                MESSAGE_EVENT.getTitle().getTitleString(), MESSAGE_EVENT.getTitle().getSubtitle() + seconds, CONFIG.getTitle().getFadeIn(),
            CONFIG.getTitle().getTitleStay(),
            CONFIG.getTitle().getFadeOut()
        );
    }

    /**
     * 执行游戏开始事件
     * 启动资源生成和指南针追踪
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {
        startResourceGenerators(gameManager);
        startCompassTracking();
    }


    /**
     * 启动资源生成器
     *
     * @param gameManager 游戏实例
     */
    private void startResourceGenerators(GameManager gameManager) {
        new GeneratorTask(gameManager).start();
    }

    /**
     * 启动指南针追踪系统
     */
    private void startCompassTracking() {
        new SpectatorCompassTask().start();
    }
}
