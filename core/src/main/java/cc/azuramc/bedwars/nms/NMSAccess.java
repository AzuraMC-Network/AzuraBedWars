package cc.azuramc.bedwars.nms;

import org.bukkit.entity.Player;

/**
 * @author An5w1r@163.com
 */
public interface NMSAccess {
    void hideArmor(Player victim, Player receiver);
    void showArmor(Player victim, Player receiver);
}
