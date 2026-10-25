package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * @author an5w1r@163.com
 */
public class BungeeUtil {

    public static void connect(Player player, String server) {
        if (player == null || server == null || server.isEmpty()) {
            return;
        }

        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteArray)) {

            out.writeUTF("Connect");
            out.writeUTF(server);

            player.sendPluginMessage(AzuraBedWars.getInstance().getPlugin(), "BungeeCord", byteArray.toByteArray());

        } catch (Exception e) {
            LoggerUtil.warn("Failed to connect player " + player.getName() + " to server " + server);
            e.printStackTrace();
        }
    }

    public static void connect(GamePlayer gamePlayer, String server) {
        Player player = gamePlayer.getPlayer();
        connect(player, server);
    }

    public static void connectToLobby(GamePlayer gamePlayer) {
        String lobbyServer = AzuraBedWars.getInstance().getSettingsConfig().getLobbyServer();
        if (lobbyServer == null || lobbyServer.isEmpty()) {
            gamePlayer.getPlayer().kickPlayer("");
            return;
        }
        connect(gamePlayer, lobbyServer);
    }
}
