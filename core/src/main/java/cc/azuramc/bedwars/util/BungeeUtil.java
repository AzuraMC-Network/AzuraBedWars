package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * @author An5w1r@163.com
 */
public class BungeeUtil {

    /**
     * 将玩家连接到指定的 BungeeCord 子服务器
     *
     * @param player 玩家对象
     * @param server 要连接的目标服务器名称
     */
    public static void connect(Player player, String server) {
        if (player == null || server == null || server.isEmpty()) {
            return;
        }

        // 发送跳服指令
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteArray)) {

            out.writeUTF("Connect");
            out.writeUTF(server);

            player.sendPluginMessage(AzuraBedWars.getInstance(), "AzuraBedWars", byteArray.toByteArray());

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
        connect(gamePlayer, "G_BedwarsLobby#1");
    }
}
