package cc.azuramc.bedwars.upgrade.upgrade.impl;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.upgrade.AbstractIUpgradeStrategy;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 锋利升级策略实现
 *
 * @author an5w1r@163.com
 */
public class SharpenedSwordsIUpgradeStrategy extends AbstractIUpgradeStrategy {

    @Override
    public String getUpgradeName() {
        return "磨刀石";
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.IRON_SWORD.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7你方所有成员的剑将永久获得锋利I附魔！");
    }

    @Override
    public boolean canUpgrade(GamePlayer gamePlayer) {
        return !gamePlayer.getGameTeam().isHasSharpnessUpgrade();
    }

    @Override
    public int getCurrentLevel(GamePlayer gamePlayer) {
        return gamePlayer.getGameTeam().isHasSharpnessUpgrade() ? 1 : 0;
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
        List<String> lore = new ArrayList<>(getDescription());
        lore.add("");

        boolean isUnlocked = gamePlayer.getGameTeam().isHasSharpnessUpgrade();
        String tierColor = isUnlocked ? "§a" : "§e";
        int price = getUpgradePrice(0);

        lore.add(tierColor + "等级 1：锋利 I，§b" + formatPrice(price, gameModeType));
        lore.add("");

        if (isUnlocked) {
            lore.add("§a已购买");
        } else {
            lore.add("§e点击购买");
        }

        return lore;
    }

    @Override
    protected boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        gameTeam.setHasSharpnessUpgrade(true);

        // 为团队所有玩家的剑添加锋利附魔
        for (GamePlayer teamPlayer : gameTeam.getAlivePlayers()) {
            Player p = teamPlayer.getPlayer();

            for (int i = 0; i < p.getInventory().getContents().length; i++) {
                ItemStack item = p.getInventory().getContents()[i];
                if (item != null && item.getType().toString().endsWith("_SWORD")) {
                    item.addEnchantment(XEnchantment.SHARPNESS.get(), 1);
                }
            }
        }

        return true;
    }
}
