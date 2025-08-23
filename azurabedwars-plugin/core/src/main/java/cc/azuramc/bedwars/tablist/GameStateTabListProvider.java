package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏状态TabList内容提供者
 * 根据游戏状态提供TabList内容
 *
 * @author an5w1r@163.com
 */
public class GameStateTabListProvider {

    private final SettingsConfig settingsConfig;

    public GameStateTabListProvider() {
        this.settingsConfig = AzuraBedWars.getInstance().getSettingsConfig();
    }

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
        SettingsConfig.WaitingState waitingState = settingsConfig.getWaitingState();
        headerFooterManager.setHeader(waitingState.getHeader());
        headerFooterManager.setFooter(waitingState.getFooter());
    }

    /**
     * 设置运行状态的内容
     */
    private void setRunningStateContent(HeaderFooterManager headerFooterManager) {
        SettingsConfig.RunningState runningState = settingsConfig.getRunningState();
        headerFooterManager.setHeader(runningState.getHeader());
        headerFooterManager.setFooter(runningState.getFooter());
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

        SettingsConfig.EndingState endingState = settingsConfig.getEndingState();

        // 处理Header中的占位符
        List<String> processedHeader = new ArrayList<>();
        for (String line : endingState.getHeader()) {
            processedHeader.add(line.replace("<gameResult>", gameResult));
        }
        headerFooterManager.setHeader(processedHeader);

        // Footer直接使用配置
        headerFooterManager.setFooter(endingState.getFooter());
    }
}
