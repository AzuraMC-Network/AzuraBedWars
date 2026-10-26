package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Lobby countdown before the game starts
 *
 * @author an5w1r@163.com
 */
public class GameStartTask extends BukkitRunnable {


    private final GameManager gameManager;

    @Getter
    private int countdown = gameStart().getDefaultCountdown();

    public GameStartTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        if (!gameManager.canStart()) {
            cancel();
            gameManager.setGameStartTask(null);
            gameManager.broadcastMessage(gameStart().getMsgNotEnoughPlayers());
            AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
            return;
        }

        if (countdown > gameStart().getQuickStartCountdown()
                && GamePlayer.getOnlinePlayers().size() >= gameManager.getMaxPlayers()) {
            countdown = gameStart().getQuickStartCountdown();
            gameManager.broadcastMessage(gameStart().getMsgGameFull());
        }

        if (isAnnouncementTime()) {
            announceCountdown();
        }

        if (countdown <= 0) {
            gameManager.start();
            // a plugin cancelled the start
            countdown = gameStart().getDefaultCountdown();
            return;
        }

        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
        float progress = Math.min(1.0F, (float) countdown / gameStart().getDefaultCountdown());
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            player.setLevel(countdown);
            player.setExp(progress);
        }

        countdown--;
    }

    private boolean isAnnouncementTime() {
        for (int announcementTime : gameStart().getAnnouncementTimes()) {
            if (countdown == announcementTime) {
                return true;
            }
        }
        return false;
    }

    private void announceCountdown() {
        for (GamePlayer player : GamePlayer.getOnlinePlayers()) {
            player.sendMessage(String.format(gameStart().getMsgCountdown(), countdown));
            player.sendTitle(String.format(gameStart().getTitleCountdown(), countdown), gameStart().getSubtitleText(),
                    gameStart().getFadeIn(), gameStart().getTitleStay(), gameStart().getFadeOut());
            player.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 1F, 10F);
        }
    }

    private static TimelineConfig.GameStartEvent gameStart() {
        return AzuraBedWars.getInstance().getTimelineConfig().getGameStartEvent();
    }
}
