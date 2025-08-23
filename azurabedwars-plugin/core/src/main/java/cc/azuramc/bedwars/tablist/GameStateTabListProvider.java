package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;

import java.util.Arrays;
import java.util.List;

/**
 * 游戏状态TabList内容提供者
 * 根据游戏状态提供TabList内容
 *
 * @author an5w1r@163.com
 */
public class GameStateTabListProvider {

    /**
     * 根据游戏阶段自动设置Header和Footer
     */
    public void updateHeaderFooterByGameState(GameManager gameManager, HeaderFooterManager headerFooterManager) {
        if (gameManager == null) {
            return;
        }

        GameState gameState = gameManager.getGameState();

        switch (gameState) {
            case WAITING:
                setWaitingStateContent(headerFooterManager);
                break;
            case RUNNING:
                setRunningStateContent(headerFooterManager);
                break;
            case ENDING:
                setEndingStateContent(gameManager, headerFooterManager);
                break;
        }
    }

    /**
     * 设置等待状态的内容
     */
    private void setWaitingStateContent(HeaderFooterManager headerFooterManager) {
        List<String> waitingHeader = Arrays.asList(
                "&b你正在 &eAzuraMC &b游玩起床战争"
        );
        headerFooterManager.setHeader(waitingHeader);

        List<String> waitingFooter = Arrays.asList(
                "&bas.azuramc.cc"
        );
        headerFooterManager.setFooter(waitingFooter);
    }

    /**
     * 设置运行状态的内容
     */
    private void setRunningStateContent(HeaderFooterManager headerFooterManager) {
        List<String> runningHeader = Arrays.asList(
                "&b你正在 &eAzuraMC &b游玩起床战争"
        );
        headerFooterManager.setHeader(runningHeader);

        List<String> runningFooter = Arrays.asList(
                "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                "&bas.azuramc.cc"
        );
        headerFooterManager.setFooter(runningFooter);
    }

    /**
     * 设置结束状态的内容
     */
    private void setEndingStateContent(GameManager gameManager, HeaderFooterManager headerFooterManager) {
        GameTeam winner = gameManager.getWinner();
        String gameResult;

        if (winner == null) {
            if (gameManager.isTie()) {
                gameResult = "Tie";
            } else {
                gameResult = "No Winner";
            }
        } else {
            gameResult = winner.getName() + " Won";
        }

        List<String> endingHeader = Arrays.asList(
                "&b你正在 &eAzuraMC &b游玩起床战争",
                "&b游戏结束 &e" + gameResult
        );
        headerFooterManager.setHeader(endingHeader);

        List<String> endingFooter = Arrays.asList(
                "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                "&bas.azuramc.cc"
        );
        headerFooterManager.setFooter(endingFooter);
    }
}
