package cc.azuramc.bedwars.upgrade.upgrade;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
public abstract class AbstractIUpgradeStrategy implements IUpgradeStrategy {

    /**
     * 升级价格缓存（升级类型 -> 等级 -> 价格）
     */
    private static final Map<String, Map<Integer, Integer>> TIER_PRICES = new HashMap<>();

    /**
     * 资源类型名称缓存
     */
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();

    static {
        // 初始化资源名称
        RESOURCE_NAMES.put(XMaterial.DIAMOND.get(), "钻石");

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
        GameModeType gameModeType = gamePlayer.getPlayerData().getMode();
        int currentLevel = getCurrentLevel(gamePlayer);
        int price = getUpgradePrice(currentLevel);

        // 处理支付
        if (!processPayment(gamePlayer, price, gameModeType)) {
            return false;
        }

        // 执行具体的升级逻辑
        boolean success = doUpgrade(gamePlayer, gameManager);

        if (success) {
            // 刷新GUI
            new TeamShopGUI(gamePlayer, gameManager).open();
        }

        return success;
    }

    @Override
    public ItemStack createUpgradeItem(GamePlayer gamePlayer, GameModeType gameModeType) {
        ItemBuilder builder = new ItemBuilder()
                .setType(getIconMaterial())
                .setDisplayName("§a" + getUpgradeName());

        // 添加Lore
        builder.setLores(getUpgradeLore(gamePlayer, gameModeType));

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
     * 处理支付
     *
     * @param gamePlayer   游戏玩家
     * @param price        价格
     * @param gameModeType 游戏模式
     * @return 是否支付成功
     */
    protected boolean processPayment(GamePlayer gamePlayer, int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return processItemPayment(gamePlayer, XMaterial.DIAMOND.get(), price);
        } else {
            return processExperiencePayment(gamePlayer, price * 100);
        }
    }

    /**
     * 处理物品支付
     *
     * @param gamePlayer 玩家
     * @param material   物品类型
     * @param amount     数量
     * @return 是否支付成功
     */
    private boolean processItemPayment(GamePlayer gamePlayer, Material material, int amount) {
        Player player = gamePlayer.getPlayer();

        // 计算玩家拥有的资源总数
        int playerTotal = 0;
        ItemStack[] inventory = player.getInventory().getContents();

        for (ItemStack item : inventory) {
            if (item != null && item.getType().equals(material)) {
                playerTotal += item.getAmount();
            }
        }

        // 检查是否有足够资源
        if (playerTotal < amount) {
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            player.sendMessage("§c没有足够资源购买！");
            return false;
        }

        // 扣除资源
        int remainingToDeduct = amount;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType().equals(material) && remainingToDeduct > 0) {
                if (item.getAmount() > remainingToDeduct) {
                    item.setAmount(item.getAmount() - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    remainingToDeduct -= item.getAmount();
                    item.setAmount(0);
                }
                player.getInventory().setItem(i, item);
            }
        }

        player.playSound(player.getLocation(), XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
    }

    /**
     * 处理经验支付
     *
     * @param gamePlayer 游戏玩家
     * @param xpLevel    经验等级
     * @return 是否支付成功
     */
    private boolean processExperiencePayment(GamePlayer gamePlayer, int xpLevel) {
        Player player = gamePlayer.getPlayer();
        if (player.getLevel() < xpLevel) {
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            player.sendMessage("§c没有足够资源购买！");
            return false;
        }

        player.setLevel(player.getLevel() - xpLevel);
        player.playSound(player.getLocation(), XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
    }

    /**
     * 格式化价格显示
     *
     * @param price        价格
     * @param gameModeType 游戏模式
     * @return 格式化后的价格字符串
     */
    protected String formatPrice(int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return price + " " + RESOURCE_NAMES.get(XMaterial.DIAMOND.get());
        } else {
            return (price * 100) + "级";
        }
    }

    /**
     * 获取罗马数字
     *
     * @param number 数字
     * @return 罗马数字表示
     */
    protected String getRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
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
