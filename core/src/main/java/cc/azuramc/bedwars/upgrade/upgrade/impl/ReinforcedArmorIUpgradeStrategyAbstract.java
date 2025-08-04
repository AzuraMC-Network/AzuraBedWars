package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractTieredIUpgradeStrategy;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 护甲升级策略实现
 *
 * @author an5w1r@163.com
 */
public class ReinforcedArmorIUpgradeStrategyAbstract extends AbstractTieredIUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "精制护甲";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.IRON_CHESTPLATE.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7己方所有成员的盔甲将获得永久保护附魔！");
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().getProtectionUpgrade();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    protected String getTierEffectDescription(int tier) {
        return "保护 " + getRomanNumeral(tier);
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getProtectionUpgrade();
        int nextLevel = currentLevel + 1;

        gameTeam.setProtectionUpgrade(nextLevel);

        // 为团队所有玩家的护甲添加保护附魔
        for (GamePlayer teamPlayer : gameTeam.getAlivePlayers()) {
            Player player = teamPlayer.getPlayer();

            for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
                ItemStack armor = player.getInventory().getArmorContents()[i];
                if (armor != null) {
                    armor.addEnchantment(XEnchantment.PROTECTION.get(), nextLevel);
                    teamPlayer.updateInventory();
                }
            }
        }

        return true;
    }
}
