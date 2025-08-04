package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractTieredIUpgradeStrategy;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 摔落保护升级策略实现
 *
 * @author an5w1r@163.com
 */
public class FallingProtectionIUpgradeStrategyAbstract extends AbstractTieredIUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "缓冲靴子";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.DIAMOND_BOOTS.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7你队伍的靴子获得了永久摔落保护！");
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().getFallingProtectionUpgrade();
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    protected String getTierEffectDescription(int tier) {
        return "缓冲靴子 " + getRomanNumeral(tier);
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getFallingProtectionUpgrade();
        int nextLevel = currentLevel + 1;

        gameTeam.setFallingProtectionUpgrade(nextLevel);

        // 为团队所有玩家的护甲添加摔落保护附魔
        gameTeam.getAlivePlayers().forEach(gamePlayers -> {
            ItemStack boots = gamePlayers.getPlayer().getInventory().getArmorContents()[0];
            if (boots != null) {
                boots.addEnchantment(XEnchantment.FEATHER_FALLING.get(), gameTeam.getFallingProtectionUpgrade());
                gamePlayers.updateInventory();
            }
        });

        return true;
    }
}
