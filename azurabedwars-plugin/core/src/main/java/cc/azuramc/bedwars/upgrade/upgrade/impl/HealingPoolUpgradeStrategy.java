package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractUpgradeStrategy;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * 治愈池升级策略实现
 *
 * @author an5w1r@163.com
 */
public class HealingPoolUpgradeStrategy extends AbstractUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "治愈池";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.BEACON.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7基地附近的队伍成员将获得生命恢复效果！");
    }

    @Override
    public boolean canUpgrade(GamePlayer gamePlayer) {
        return !gamePlayer.getGameTeam().getUpgradeManager().hasHealPoolUpgrade();
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().getUpgradeManager().hasHealPoolUpgrade() ? 1 : 0;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getUpgradePrice(int currentLevel) {
        return getPrice(getUpgradeName(), currentLevel);
    }

    @Override
    public List<String> getUpgradeLore(GamePlayer gamePlayer, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();

        boolean isUnlocked = gamePlayer.getGameTeam().getUpgradeManager().hasHealPoolUpgrade();

        if (!isUnlocked) {
            int price = getUpgradePrice(0);
            lore.addAll(getDescription());
            lore.add("");
            lore.add("§7价格: §b" + formatPrice(price, gameModeType));
            lore.add("");
            lore.add("§e点击购买");
        } else {
            lore.add("§7基地附近的队伍成员获得");
            lore.add("§7生命恢复效果");
            lore.add("");
            lore.add("§a已激活");
        }

        return lore;
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        gameTeam.getUpgradeManager().setHealPoolUpgrade(true);

        return true;
    }
}
