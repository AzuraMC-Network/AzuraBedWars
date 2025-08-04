package cc.azuramc.bedwars.upgrade.trap.impl;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.trap.AbstractITrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.TrapManager;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

import java.util.List;

/**
 * 反击陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class FightBackITrapStrategy extends AbstractITrapStrategy {

    @Override
    public String getDisplayName() {
        return "反击陷阱";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.FEATHER.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of(
                "§7赋予基地附近的队友速度 II 与跳跃提升 II",
                "§7效果，持续15秒。"
        );
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.FIGHT_BACK) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.FIGHT_BACK;
    }
}
