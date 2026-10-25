package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.spectator.SpectatorManager;
import cc.azuramc.bedwars.game.task.RespawnTask;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerRespawnListener implements Listener {


    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer());
        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        gamePlayer.cleanState();
        event.setRespawnLocation(gameManager.getRespawnLocation());

        if (gameTeam.isDestroyed()) {
            // spectator setup does not stick during the respawn event
            Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance().getPlugin(), () -> {
                sendPlayAgainMessage(gamePlayer);
                SpectatorManager.toSpectator(gamePlayer, messages().getDeathPermanentTitle(), messages().getDeathPermanentSubTitle());
            }, 1L);
            return;
        }

        new RespawnTask(gameManager, gamePlayer, RespawnTask.defaultSeconds()).start();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        GamePlayer gamePlayer = GamePlayer.get((Player) event.getEntity());
        if (gamePlayer != null && gamePlayer.isRespawnProtected()) {
            event.setCancelled(true);
        }
    }

    private void sendPlayAgainMessage(GamePlayer gamePlayer) {
        TextComponent textComponent = new TextComponent(messages().getPlayAgainMessage());
        TextComponent button = new TextComponent(messages().getPlayAgainButton());
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, AzuraBedWars.getInstance().getSettingsConfig().getPlayAgainCommand()));
        textComponent.addExtra(button);
        gamePlayer.getPlayer().spigot().sendMessage(textComponent);
    }

    private static MessageConfig messages() {
        return AzuraBedWars.getInstance().getMessageConfig();
    }
}
