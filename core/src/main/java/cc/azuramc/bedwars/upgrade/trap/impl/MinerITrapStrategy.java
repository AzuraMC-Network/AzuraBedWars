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
 * 挖掘疲劳陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class MinerITrapStrategy extends AbstractITrapStrategy {

    @Override
    public String getDisplayName() {
        return "挖掘疲劳陷阱";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.IRON_PICKAXE.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7造成挖掘疲劳效果，持续8秒。");
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.MINER) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.MINER;
    }
}
