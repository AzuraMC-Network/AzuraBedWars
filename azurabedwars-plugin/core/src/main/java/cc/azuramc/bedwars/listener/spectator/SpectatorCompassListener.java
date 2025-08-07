package cc.azuramc.bedwars.listener.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author An5w1r@163.com
 */
public class SpectatorCompassListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Material interactingMaterial = event.getMaterial();

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }


        handleSpectatorCompassNavigation(event, gamePlayer, interactingMaterial);
    }

    /**
     * 处理旁观者使用指南针的导航功能
     */
    private void handleSpectatorCompassNavigation(PlayerInteractEvent event, GamePlayer gamePlayer, Material material) {
        // 检查是否为左键点击动作
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR ||
                event.getAction() == Action.LEFT_CLICK_BLOCK;

        // 检查是否有旁观目标且物品是指南针
        boolean hasSpectatorTargetWithCompass = gamePlayer.getSpectatorTarget() != null &&
                material == XMaterial.COMPASS.get();

        // 如果同时满足上述条件，执行传送
        if (isLeftClick && hasSpectatorTargetWithCompass) {
            gamePlayer.getSpectatorTarget().tp();
        }
    }
}
