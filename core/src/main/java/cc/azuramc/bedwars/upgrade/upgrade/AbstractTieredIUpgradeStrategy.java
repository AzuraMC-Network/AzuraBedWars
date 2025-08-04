package cc.azuramc.bedwars.upgrade.upgrade;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 分级升级策略抽象类
 * 处理有多个等级的升级类型
 *
 * @author an5w1r@163.com
 */
public abstract class AbstractTieredIUpgradeStrategy extends AbstractIUpgradeStrategy {

    @Override
    public boolean canUpgrade(GamePlayer gamePlayer) {
        return getCurrentLevel(gamePlayer) < getMaxLevel();
    }

    @Override
    public int getUpgradePrice(int currentLevel) {
        return getPrice(getUpgradeName(), currentLevel);
    }

    @Override
    public List<String> getUpgradeLore(GamePlayer gamePlayer, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>(getDescription());
        lore.add("");

        int currentLevel = getCurrentLevel(gamePlayer);

        // 添加所有等级的描述和价格
        for (int tier = 0; tier < getMaxLevel(); tier++) {
            String tierColor;
            if (tier < currentLevel) {
                // 已购买的等级
                tierColor = "§a";
            } else if (tier == currentLevel) {
                // 当前可购买的等级
                tierColor = "§e";
            } else {
                // 未解锁的等级
                tierColor = "§7";
            }

            int displayTier = tier + 1;
            int price = getUpgradePrice(tier);

            String effectDescription = getTierEffectDescription(displayTier);
            lore.add(tierColor + "等级 " + displayTier + "：" + effectDescription +
                    "，§b" + formatPrice(price, gameModeType));
        }

        lore.add("");

        if (currentLevel < getMaxLevel()) {
            lore.add("§e点击升级到等级 " + getRomanNumeral(currentLevel + 1));
        } else {
            lore.add("§a已达到最高等级");
        }

        return lore;
    }

    /**
     * 获取等级效果描述
     *
     * @param tier 等级
     * @return 效果描述
     */
    protected abstract String getTierEffectDescription(int tier);
}
