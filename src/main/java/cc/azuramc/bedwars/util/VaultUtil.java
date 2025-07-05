package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;

/**
 * @author an5w1r@163.com
 */
public class VaultUtil {

    public static boolean ecoIsNull = AzuraBedWars.getInstance().getEcon() == null;

    public static boolean chatIsNull = AzuraBedWars.getInstance().getChat() == null;

    public static void depositPlayer(GamePlayer gamePlayer, double amount) {
        AzuraBedWars.getInstance().getEcon().depositPlayer(gamePlayer.getPlayer(), amount);
    }
}
