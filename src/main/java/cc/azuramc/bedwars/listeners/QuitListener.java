package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        game.removePlayers(GamePlayer.get(event.getPlayer().getUniqueId()));
    }
}
