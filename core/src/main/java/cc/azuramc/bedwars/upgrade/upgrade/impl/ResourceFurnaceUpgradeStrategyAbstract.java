package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractTieredUpgradeStrategy;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

import java.util.List;

/**
 * 资源炉升级策略实现
 *
 * @author an5w1r@163.com
 */
public class ResourceFurnaceUpgradeStrategyAbstract extends AbstractTieredUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "铁锻炉";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.FURNACE.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7升级你岛屿资源池的生成速度和和最大容量.");
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().getResourceFurnaceUpgrade();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    protected String getTierEffectDescription(int tier) {
        return switch (tier) {
            case 1 -> "+50%资源";
            case 2 -> "+100%资源";
            case 3 -> "生成绿宝石";
            case 4 -> "+200%资源";
            default -> "";
        };
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getResourceFurnaceUpgrade();
        int nextLevel = currentLevel + 1;

        gameTeam.setResourceFurnaceUpgrade(nextLevel);
        LoggerUtil.debug("资源炉升级到等级 " + nextLevel + "，当前价格: " + getUpgradePrice(currentLevel));

        return true;
    }
}
