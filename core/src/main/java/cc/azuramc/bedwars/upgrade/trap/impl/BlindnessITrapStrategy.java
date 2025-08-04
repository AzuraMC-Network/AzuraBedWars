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
 * 失明陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class BlindnessITrapStrategy extends AbstractITrapStrategy {

    @Override
    public String getTrapType() {
        return "BLINDNESS";
    }

    @Override
    public String getDisplayName() {
        return "致盲陷阱";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.TRIPWIRE_HOOK.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7造成失明与缓慢效果，持续8秒。");
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.BLINDNESS) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.BLINDNESS;
    }
}
