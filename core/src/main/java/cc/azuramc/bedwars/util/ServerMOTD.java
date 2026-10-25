package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.game.GameState;

/**
 * @author an5w1r@163.com
 */
public class ServerMOTD {

    private static GameState currentGameState = GameState.WAITING;

    /**
     * 根据游戏状态更新服务器MOTD
     */
    public static void updateMOTD(GameState gameState) {
        currentGameState = gameState;
    }

    /**
     * 获取当前游戏状态对应的MOTD
     *
     * @return 当前游戏状态的MOTD字符串
     */
    public static String getMOTD() {
        switch (currentGameState) {
            case RUNNING:
                return "abw-running";
            case WAITING:
                return "abw-waiting";
            default:
                return "abw-ending";
        }
    }
}
