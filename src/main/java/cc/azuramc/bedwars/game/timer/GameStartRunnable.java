package cc.azuramc.bedwars.game.timer;

import cc.azuramc.bedwars.enums.GameState;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 游戏大厅倒计时
 * <p>
 * 负责管理游戏开始前的倒计时逻辑，包括时间提醒、人数检查和经验条显示
 * </p>
 */
public class GameStartRunnable extends BukkitRunnable {
    // 倒计时常量
    private static final int DEFAULT_COUNTDOWN = 120; // 默认倒计时时间(秒)
    private static final int QUICK_START_COUNTDOWN = 10; // 人满时的快速开始时间
    
    // 公告时间点
    private static final int[] ANNOUNCEMENT_TIMES = {60, 30, 5, 4, 3, 2, 1};
    
    // 标题显示配置
    private static final int TITLE_FADE_IN = 1;
    private static final int TITLE_STAY = 20;
    private static final int TITLE_FADE_OUT = 1;
    
    // 消息颜色
    private static final String MESSAGE_PREFIX = "§e";
    private static final String TIME_COLOR = "§c";
    private static final String ERROR_COLOR = "§c";
    
    // 消息模板
    private static final String MSG_COUNTDOWN = MESSAGE_PREFIX + "游戏将在" + TIME_COLOR + "%d" + MESSAGE_PREFIX + "秒后开始！";
    private static final String MSG_NOT_ENOUGH_PLAYERS = ERROR_COLOR + "人数不足，取消倒计时！";
    private static final String MSG_GAME_FULL = MESSAGE_PREFIX + "游戏人数已满," + QUICK_START_COUNTDOWN + "秒后开始游戏！";
    private static final String TITLE_COUNTDOWN = TIME_COLOR + "§l%d";
    private static final String SUBTITLE_TEXT = "§e§l准备战斗吧！";

    private final GameManager gameManager;
    
    @Getter
    private int countdown = DEFAULT_COUNTDOWN;
    
    // 是否处于快速开始状态
    private boolean isQuickStartMode = false;

    /**
     * 创建游戏大厅倒计时
     *
     * @param gameManager 游戏实例
     */
    public GameStartRunnable(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        // 处理强制开始
        if (gameManager.isForceStart()) {
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
        for (int announcementTime : ANNOUNCEMENT_TIMES) {
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
        gameManager.broadcastMessage(MSG_NOT_ENOUGH_PLAYERS);
        countdown = DEFAULT_COUNTDOWN;
        gameManager.setGameState(GameState.WAITING);
        gameManager.setGameStartRunnable(null);
        LobbyBoard.updateBoard();
        cancel();
    }

    /**
     * 发送倒计时公告
     */
    private void sendCountdownAnnouncement() {
        for (GamePlayer player : GamePlayer.getOnlinePlayers()) {
            player.sendMessage(String.format(MSG_COUNTDOWN, countdown));
            player.sendTitle(
                TITLE_FADE_IN, 
                TITLE_STAY, 
                TITLE_FADE_OUT, 
                String.format(TITLE_COUNTDOWN, countdown), 
                SUBTITLE_TEXT
            );
            player.playSound(SoundUtil.LEVEL_UP(), 1F, 10F);
        }
    }

    /**
     * 检查是否应该快速开始
     */
    private void checkForQuickStart() {
        boolean isFull = GamePlayer.getOnlinePlayers().size() >= gameManager.getMaxPlayers();
        
        if (!isQuickStartMode && isFull && countdown > QUICK_START_COUNTDOWN) {
            isQuickStartMode = true;
            countdown = QUICK_START_COUNTDOWN;
            gameManager.broadcastMessage(MSG_GAME_FULL);
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
        LobbyBoard.updateBoard();
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
        if (countdown >= DEFAULT_COUNTDOWN) {
            return 1.0F;
        } else {
            return (float) countdown / DEFAULT_COUNTDOWN;
        }
    }
}
