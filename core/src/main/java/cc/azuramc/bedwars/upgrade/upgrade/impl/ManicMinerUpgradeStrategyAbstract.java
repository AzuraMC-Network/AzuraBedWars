package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractTieredUpgradeStrategy;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

import java.util.List;

/**
 * 疯狂矿工升级策略实现
 *
 * @author an5w1r@163.com
 */
public class ManicMinerUpgradeStrategyAbstract extends AbstractTieredUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "疯狂矿工";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.GOLDEN_PICKAXE.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7己方所有成员获得急迫效果。");
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().getUpgradeManager().getMagicMinerUpgrade();
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    protected String getTierEffectDescription(int tier) {
        return "急迫 " + getRomanNumeral(tier);
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getUpgradeManager().getMagicMinerUpgrade();
        int nextLevel = currentLevel + 1;

        gameTeam.getUpgradeManager().setMagicMinerUpgrade(nextLevel);

        return true;
    }
}
