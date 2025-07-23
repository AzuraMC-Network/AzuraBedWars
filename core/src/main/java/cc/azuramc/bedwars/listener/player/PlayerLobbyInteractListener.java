package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.gui.ModeSelectionGUI;
import cc.azuramc.bedwars.gui.TeamSelectionGUI;
import cc.azuramc.bedwars.util.BungeeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author An5w1r@163.com
 */
public class PlayerLobbyInteractListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Material interactingMaterial = event.getMaterial();

        if (gameManager.getGameState() == GameState.WAITING) {
            handleWaitingState(event, gamePlayer, interactingMaterial);
        }
    }

    /**
     * 处理等待状态下的交互事件
     */
    private void handleWaitingState(PlayerInteractEvent event, GamePlayer gamePlayer, Material interactingMaterial) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            switch (interactingMaterial) {
                case PAPER:
                    new ModeSelectionGUI(gamePlayer).open();
                    break;
                case SLIME_BALL:
                    BungeeUtil.connectToLobby(gamePlayer);
                    break;
                case RED_WOOL, YELLOW_WOOL, BLUE_WOOL, GREEN_WOOL, WHITE_WOOL:
                    new TeamSelectionGUI(gamePlayer).open();
                    break;
                default:
                    break;
            }
        }
    }
}
