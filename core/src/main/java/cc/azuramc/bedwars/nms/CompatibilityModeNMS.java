package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class CompatibilityModeNMS implements NMSAccess {

    @Override
    public void hideArmor(Player victim, Player receiver) {
        LoggerUtil.debug("compatibility mode");
    }

    @Override
    public void showArmor(Player victim, Player receiver) {

    }

    @Override
    public void hideArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {

    }

    @Override
    public void showArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {

    }
}
