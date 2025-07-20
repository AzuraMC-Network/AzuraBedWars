package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @author An5w1r@163.com
 */
public interface NMSAccess {
    void hideArmor(Player victim, Player receiver);
    void showArmor(Player victim, Player receiver);
    void hideArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList);
    void showArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList);
    Fireball setFireballDirection(Fireball fireball, Vector vector);
}
