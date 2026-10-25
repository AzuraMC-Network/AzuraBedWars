package cc.azuramc.bedwars.upgrade;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
@Getter
public enum TeamUpgrade {
    SHARPENED_SWORDS("磨刀石", XMaterial.IRON_SWORD, 10, "§7你方所有成员的剑将永久获得锋利I附魔！",
            new int[]{8}, "锋利 I"),
    REINFORCED_ARMOR("精制护甲", XMaterial.IRON_CHESTPLATE, 11, "§7己方所有成员的盔甲将获得永久保护附魔！",
            new int[]{5, 10, 20, 30}, "保护 I", "保护 II", "保护 III", "保护 IV"),
    MANIC_MINER("疯狂矿工", XMaterial.GOLDEN_PICKAXE, 12, "§7己方所有成员获得急迫效果。",
            new int[]{4, 6}, "急迫 I", "急迫 II"),
    RESOURCE_FURNACE("铁锻炉", XMaterial.FURNACE, 19, "§7升级你岛屿资源池的生成速度和和最大容量.",
            new int[]{4, 8, 12, 16}, "+50%资源", "+100%资源", "生成绿宝石", "+200%资源"),
    HEALING_POOL("治愈池", XMaterial.BEACON, 20, "§7基地附近的队伍成员将获得生命恢复效果！",
            new int[]{3}, "生命恢复"),
    CUSHIONED_BOOTS("缓冲靴子", XMaterial.DIAMOND_BOOTS, 21, "§7你队伍的靴子获得了永久摔落保护！",
            new int[]{2, 4}, "缓冲靴子 I", "缓冲靴子 II");

    private final String displayName;
    private final XMaterial icon;
    private final int slot;
    private final String description;
    /** Diamonds for each level, index is the level already owned */
    private final int[] prices;
    private final String[] tierNames;

    TeamUpgrade(String displayName, XMaterial icon, int slot, String description, int[] prices, String... tierNames) {
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.description = description;
        this.prices = prices;
        this.tierNames = tierNames;
    }

    public int getMaxLevel() {
        return prices.length;
    }
}
