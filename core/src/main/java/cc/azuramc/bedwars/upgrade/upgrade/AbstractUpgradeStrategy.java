package cc.azuramc.bedwars.upgrade.upgrade;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import com.cryptomorin.xseries.XEnchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象升级策略基类
 * 提供公共的升级逻辑
 *
 * @author an5w1r@163.com
 */
public abstract class AbstractUpgradeStrategy implements UpgradeStrategy {

    /**
     * 升级价格缓存（升级类型 -> 等级 -> 价格）
     */
    private static final Map<String, Map<Integer, Integer>> TIER_PRICES = new HashMap<>();

    static {
        // 初始化各种升级的价格
        initializePrices();
    }

    /**
     * 初始化价格配置
     */
    private static void initializePrices() {
        // 锋利升级价格
        Map<Integer, Integer> swordPrices = new HashMap<>();
        swordPrices.put(0, 8);
        TIER_PRICES.put("磨刀石", swordPrices);

        // 护甲保护价格
        Map<Integer, Integer> armorPrices = new HashMap<>();
        armorPrices.put(0, 5);
        armorPrices.put(1, 10);
        armorPrices.put(2, 20);
        armorPrices.put(3, 30);
        TIER_PRICES.put("精制护甲", armorPrices);

        // 疯狂矿工价格
        Map<Integer, Integer> minerPrices = new HashMap<>();
        minerPrices.put(0, 4);
        minerPrices.put(1, 6);
        TIER_PRICES.put("疯狂矿工", minerPrices);

        // 资源炉价格
        Map<Integer, Integer> furnacePrices = new HashMap<>();
        furnacePrices.put(0, 4);
        furnacePrices.put(1, 8);
        furnacePrices.put(2, 12);
        furnacePrices.put(3, 16);
        TIER_PRICES.put("铁锻炉", furnacePrices);

        // 治愈池价格
        Map<Integer, Integer> healingPrices = new HashMap<>();
        healingPrices.put(0, 3);
        TIER_PRICES.put("治愈池", healingPrices);

        // 摔落保护价格
        Map<Integer, Integer> fallingPrices = new HashMap<>();
        fallingPrices.put(0, 2);
        fallingPrices.put(1, 4);
        TIER_PRICES.put("缓冲靴子", fallingPrices);
    }

    @Override
    public boolean performUpgrade(GamePlayer gamePlayer, GameManager gameManager) {
        // 使用UpgradeManager作为同步锁确保升级操作的原子性
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return false;
        }
        synchronized (gameTeam.getUpgradeManager()) {
            // 再次检查是否可以升级
            if (!canUpgrade(gamePlayer)) {
                return false;
            }

            // 先进行支付
            PriceCost price = PriceCost.teamUpgrade(getUpgradePrice(getCurrentLevel(gamePlayer)));
            if (!AzuraBedWars.getInstance().getGameManager().getEconomy().charge(gamePlayer, price)) {
                return false;
            }

            // 执行具体的升级逻辑
            if (!doUpgrade(gamePlayer, gameManager)) {
                return false;
            }
        }

        // 升级成功，刷新GUI
        new TeamShopGUI(gamePlayer, gameManager).open();
        return true;
    }

    @Override
    public ItemStack createUpgradeItem(GamePlayer gamePlayer) {
        ItemBuilder builder = new ItemBuilder()
                .setType(getIconMaterial())
                .setDisplayName("§a" + getUpgradeName());

        // 添加Lore
        builder.setLores(getUpgradeLore(gamePlayer));

        // 如果已升级到最高级，添加附魔效果
        if (!canUpgrade(gamePlayer)) {
            builder.addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        // 添加隐藏属性标志
        builder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        return builder.getItem();
    }

    /**
     * 执行具体的升级逻辑
     *
     * @param gamePlayer  游戏玩家
     * @param gameManager 游戏管理器
     * @return 是否升级成功
     */
    protected abstract boolean doUpgrade(GamePlayer gamePlayer, GameManager gameManager);

    /**
     * 格式化价格显示
     *
     * @param price 价格
     * @return 格式化后的价格字符串
     */
    protected String formatPrice(int price) {
        return AzuraBedWars.getInstance().getGameManager().getEconomy().formatPrice(PriceCost.teamUpgrade(price));
    }

    /**
     * 获取罗马数字
     *
     * @param number 数字
     * @return 罗马数字表示
     */
    protected String getRomanNumeral(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            default:
                return String.valueOf(number);
        }
    }

    /**
     * 获取升级价格
     *
     * @param upgradeName 升级名称
     * @param level       等级
     * @return 价格
     */
    protected int getPrice(String upgradeName, int level) {
        Map<Integer, Integer> prices = TIER_PRICES.get(upgradeName);
        return prices != null ? prices.getOrDefault(level, 0) : 0;
    }
}
