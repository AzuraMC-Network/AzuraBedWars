package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 游戏大厅倒计时
 * 负责管理游戏开始前的倒计时逻辑，包括时间提醒、人数检查和经验条显示
 *
 * @author an5w1r@163.com
 */
public class GameStartTask extends BukkitRunnable {

    private static final EventSettingsConfig.GameStartEvent gameStartConfig = AzuraBedWars.getInstance().getEventSettingsConfig().getGameStartEvent();

    private final GameManager gameManager;

    @Getter
    private int countdown = gameStartConfig.getDefaultCountdown();

    /**
     * 是否处于快速开始状态
     */
    private boolean isQuickStartMode = false;

    /**
     * 创建游戏大厅倒计时
     *
     * @param gameManager 游戏实例
     */
    public GameStartTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        // 处理强制开始
        if (gameManager.isForceStarted()) {
            cancel();
            return;
        }

        // 检查玩家数量是否足够
        if (!gameManager.hasEnoughPlayers()) {
            handleNotEnoughPlayers();
            return;
        }

        // 处理特定时间点的提示
        if (isAnnouncementTime(countdown)) {
            sendCountdownAnnouncement();
        }

        // 检查是否人满可以快速开始
        checkForQuickStart();

        // 倒计时结束，开始游戏
        if (countdown <= 0) {
            startGame();
            return;
        }

        // 更新记分板
        updateGameState();

        // 减少倒计时
        --countdown;
    }

    /**
     * 判断当前时间是否需要公告
     *
     * @param time 当前倒计时时间
     * @return 是否需要公告
     */
    private boolean isAnnouncementTime(int time) {
        for (int announcementTime : gameStartConfig.getAnnouncementTimes()) {
            if (time == announcementTime) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理玩家不足的情况
     */
    private void handleNotEnoughPlayers() {
        isQuickStartMode = false;
        gameManager.broadcastMessage(gameStartConfig.getMsgNotEnoughPlayers());
        countdown = gameStartConfig.getDefaultCountdown();
        gameManager.setGameState(GameState.WAITING);
        gameManager.setGameStartTask(null);
        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
        cancel();
    }

    /**
     * 发送倒计时公告
     */
    private void sendCountdownAnnouncement() {
        for (GamePlayer player : GamePlayer.getOnlinePlayers()) {
            player.sendMessage(String.format(gameStartConfig.getMsgCountdown(), countdown));
            player.sendTitle(String.format(gameStartConfig.getTitleCountdown(), countdown), gameStartConfig.getSubtitleText(),
                    gameStartConfig.getFadeIn(), gameStartConfig.getTitleStay(), gameStartConfig.getFadeOut());
            player.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 1F, 10F);
        }
    }

    /**
     * 检查是否应该快速开始
     */
    private void checkForQuickStart() {
        boolean isFull = GamePlayer.getOnlinePlayers().size() >= gameManager.getMaxPlayers();

        if (!isQuickStartMode && isFull && countdown > gameStartConfig.getDefaultCountdown()) {
            isQuickStartMode = true;
            countdown = gameStartConfig.getQuickStartCountdown();
            gameManager.broadcastMessage(gameStartConfig.getMsgGameFull());
        }
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        cancel();
        gameManager.start();
    }

    /**
     * 更新游戏状态和玩家体验条
     */
    private void updateGameState() {
        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
        updatePlayerExperience();
    }

    /**
     * 更新玩家经验条和等级显示
     */
    private void updatePlayerExperience() {
        float progressPercentage = calculateProgress();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            player.setLevel(countdown);
            player.setExp(progressPercentage);
        }
    }

    /**
     * 计算倒计时进度
     *
     * @return 进度百分比 (0.0-1.0)
     */
    private float calculateProgress() {
        if (countdown >= gameStartConfig.getDefaultCountdown()) {
            return 1.0F;
        } else {
            return (float) countdown / gameStartConfig.getDefaultCountdown();
        }
    }
}
