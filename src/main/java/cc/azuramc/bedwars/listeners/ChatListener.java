package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        int level = (playerData.getKills() * 2) + (playerData.getDestroyedBeds() * 10) + (playerData.getWins() * 15);
        String globalPrefix = ChatColor.translateAlternateColorCodes('&', AzuraBedWars.getInstance().getChat().getPlayerPrefix(player));

        if (game.getGameState() == GameState.RUNNING && !game.getEventManager().isOver()) {
            if (gamePlayer.isSpectator()) {
                String text = "§7[旁观者]" + "§f" + gamePlayer.getDisplayname() + "§7: " + message;
                if (player.hasPermission("bw.*")) {
                    game.broadcastMessage(text);
                    return;
                }

                game.broadcastSpectatorMessage(text);
                return;
            }

            GameTeam gameTeam = gamePlayer.getGameTeam();
            boolean all = event.getMessage().startsWith("!");
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(all ? "§6[全局]" : "§9[团队]");
            stringBuilder.append("§6[").append(AzuraBedWars.getInstance().getLevel(level)).append("✫]");
            stringBuilder.append(globalPrefix);
            stringBuilder.append(gameTeam.getChatColor()).append("[").append(gameTeam.getName()).append("]");
            stringBuilder.append(gamePlayer.getDisplayname());
            stringBuilder.append("§7: ").append(all ? message.substring(1) : message);


            if (all) {
                game.broadcastMessage(stringBuilder.toString());
            } else {
                game.broadcastTeamMessage(gameTeam, stringBuilder.toString());
            }
            return;
        }

        game.broadcastMessage("§6[" + AzuraBedWars.getInstance().getLevel(level) + "✫]" + globalPrefix + "§7" + gamePlayer.getDisplayname() + ": " + message);
    }
}
