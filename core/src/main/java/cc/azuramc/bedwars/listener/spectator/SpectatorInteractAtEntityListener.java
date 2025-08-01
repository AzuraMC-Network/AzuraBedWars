package cc.azuramc.bedwars.listener.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.spectator.SpectatorSettings;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * @author An5w1r@163.com
 */
public class SpectatorInteractAtEntityListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && gameManager.getGameState() == GameState.RUNNING) {
            handleSpectatorEntityInteraction(event, gamePlayer);
        }
    }

    /**
     * 处理旁观者与实体的交互
     */
    private void handleSpectatorEntityInteraction(PlayerInteractAtEntityEvent event, GamePlayer gamePlayer) {
        if (event.getRightClicked() instanceof Player targetPlayer && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRST_PERSON)) {
            event.setCancelled(true);

            if (GamePlayer.get(targetPlayer.getUniqueId()).isSpectator()) {
                return;
            }

            enableFirstPersonSpectating(gamePlayer, event.getPlayer(), targetPlayer);
            return;
        }
        event.setCancelled(true);
    }

    /**
     * 启用第一人称旁观模式
     */
    private void enableFirstPersonSpectating(GamePlayer gamePlayer, Player spectator, Player target) {
        gamePlayer.sendTitle("§a正在旁观§7" + target.getName(), "§a点击左键打开菜单  §c按Shift键退出", 0, 20, 0);
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setSpectatorTarget(target);
    }
}
