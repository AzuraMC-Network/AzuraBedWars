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
 * 警报陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class AlarmITrapStrategy extends AbstractITrapStrategy {

    @Override
    public String getTrapType() {
        return "ALARM";
    }

    @Override
    public String getDisplayName() {
        return "警报陷阱";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.REDSTONE_TORCH.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of(
                "§7显示隐身的玩家，",
                "§7及其名称与队伍名。"
        );
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.ALARM) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.ALARM;
    }
}
