package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Bukkit;

/**
 * @author an5w1r@163.com
 */
public class ServerMOTD {

    private final GameManager gameManager;

    public ServerMOTD() {
        this.gameManager = AzuraBedWars.getInstance().getGameManager();
        updateMOTD();
    }

    /**
     * 根据游戏状态更新服务器MOTD
     */
    public void updateMOTD() {
        GameState gameState = gameManager.getGameState();

        switch (gameState) {
            case RUNNING:
                Bukkit.setMotd("abw-running");
                break;
            case WAITING:
                Bukkit.setMotd("abw-waiting");
                break;
            default:
                Bukkit.setMotd("abw-unknown");
                break;
        }
    }
}