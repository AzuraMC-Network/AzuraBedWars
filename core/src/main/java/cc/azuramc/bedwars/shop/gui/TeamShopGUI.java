package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.upgrade.factory.TrapStrategyFactory;
import cc.azuramc.bedwars.upgrade.factory.UpgradeStrategyFactory;
import cc.azuramc.bedwars.upgrade.trap.TrapManager;
import cc.azuramc.bedwars.upgrade.trap.TrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import cc.azuramc.bedwars.upgrade.upgrade.UpgradeStrategy;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * 团队商店GUI
 * 使用策略模式重构，消除重复代码
 *
 * @author an5w1r@163.com
 */
public class TeamShopGUI extends CustomGUI {

    /** 灰色 */
    private static final int BORDER_GLASS_COLOR = 7;

    /**
     * 创建团队商店GUI
     * @param gamePlayer 玩家
     * @param gameManager 游戏实例
     */
    public TeamShopGUI(GamePlayer gamePlayer, GameManager gameManager) {
        super(gamePlayer, "§8团队升级", 54);

        if (gamePlayer.getPlayerData() == null) {
            return;
        }
        GameModeType gameModeType = gamePlayer.getPlayerData().getMode();

        // 设置界面边框
        setupBorders();

        // 设置升级选项
        setupUpgradeOptions(gamePlayer, gameManager, gameModeType);

        // 设置陷阱选项
        setupTrapOptions(gamePlayer, gameManager, gameModeType);

        // 陷阱列表
        setupTrapList(gamePlayer);
    }

    /**
     * 设置界面边框
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 27; i < 36; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 设置升级选项
     */
    private void setupUpgradeOptions(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        Map<String, UpgradeStrategy> strategies = UpgradeStrategyFactory.getAllStrategies();

        // 升级选项的槽位映射
        int[] upgradeSlots = {10, 11, 12, 19, 20, 21};
        String[] upgradeNames = {"磨刀石", "精制护甲", "疯狂矿工", "铁锻炉", "治愈池", "缓冲靴子"};

        for (int i = 0; i < upgradeNames.length; i++) {
            UpgradeStrategy strategy = strategies.get(upgradeNames[i]);
            if (strategy != null) {
                setupUpgradeItem(upgradeSlots[i], strategy, gamePlayer, gameManager, gameModeType);
            }
        }
    }

    /**
     * 设置单个升级选项
     */
    private void setupUpgradeItem(int slot, UpgradeStrategy strategy, GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        ItemStack item = strategy.createUpgradeItem(gamePlayer, gameModeType);

        GUIAction action = new GUIAction(0, () -> {
            if (strategy.canUpgrade(gamePlayer)) {
                strategy.performUpgrade(gamePlayer, gameManager);
            }
        }, false);

        setItem(slot, item, action);
    }

    /**
     * 设置陷阱选项
     */
    private void setupTrapOptions(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        Map<TrapType, TrapStrategy> strategies = TrapStrategyFactory.getAllStrategies();

        // 陷阱选项的槽位映射
        int[] trapSlots = {14, 15, 16, 23};
        TrapType[] trapTypes = {TrapType.BLINDNESS, TrapType.FIGHT_BACK, TrapType.ALARM, TrapType.MINER};

        for (int i = 0; i < trapTypes.length; i++) {
            TrapStrategy strategy = strategies.get(trapTypes[i]);
            if (strategy != null) {
                setupTrapItem(trapSlots[i], strategy, gamePlayer, gameManager, gameModeType);
            }
        }
    }

    /**
     * 设置单个陷阱选项
     */
    private void setupTrapItem(int slot, TrapStrategy strategy, GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        ItemStack item = strategy.createTrapItem(gamePlayer, gameModeType);

        GUIAction action = new GUIAction(0, () -> {
            if (strategy.canPurchase(gamePlayer)) {
                strategy.performPurchase(gamePlayer, gameManager);
            }
        }, false);

        setItem(slot, item, action);
    }

    /**
     * 设置陷阱列表
     */
    private void setupTrapList(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }
        TrapManager trapManager = gameTeam.getTrapManager();
        if (trapManager == null) {
            return;
        }

        // 设置空的陷阱槽位
        for (int i = 39 + trapManager.getActiveTrapCount(); i < 42; i++) {
            setItem(i, new ItemBuilder()
                            .setType(XMaterial.matchXMaterial("STAINED_GLASS:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS).get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§c陷阱#" + (i - 38) + ": 没有陷阱！")
                            .setLores(
                                    "§7第" + getChineseNumber(i - 38) + "个敌人进入己方基地后",
                                    "§7会触发该陷阱！",
                                    "",
                                    "§7购买的陷阱会再次排列，具体",
                                    "§7费用取决已排列的陷阱数量。",
                                    "",
                                    "§7下一个陷阱: §b" + formatPrice(trapManager.getCurrentTrapPrice(), gamePlayer.getGameModeType())
                            )
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }

        // 设置已激活的陷阱
        if (trapManager.getActiveTrapCount() >= 1) {
            for (int j = 39; j < trapManager.getActiveTrapCount() + 39; j++) {
                TrapType trapType = trapManager.getActiveTraps().get(j - 39);
                TrapStrategy strategy = TrapStrategyFactory.getStrategy(trapType);

                if (strategy != null) {
                    ItemStack item = new ItemBuilder()
                            .setType(strategy.getIconMaterial())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a陷阱#" + (j - 38) + ": " + strategy.getDisplayName())
                            .setLores(
                                    "§7第" + getChineseNumber(j - 38) + "个敌人进入己方基地后",
                                    "§7会触发该陷阱！",
                                    "",
                                    "§7购买的陷阱会再次排列，具体",
                                    "§7费用取决已排列的陷阱数量。",
                                    "",
                                    "§7下一个陷阱: §b" + formatPrice(trapManager.getCurrentTrapPrice(), gamePlayer.getGameModeType())
                            )
                            .getItem();

                    setItem(j, item, new GUIAction(0, () -> {
                    }, false));
                }
            }
        }
    }

    /**
     * 格式化价格显示
     * @param price 价格
     * @param gameModeType 游戏模式
     * @return 格式化后的价格字符串
     */
    private String formatPrice(int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return price + " 钻石";
        } else {
            return (price * 100) + "级";
        }
    }

    /**
     * 获取数字对应的中文
     * @param number 数字
     * @return 中文汉字数字
     */
    private String getChineseNumber(int number) {
        return switch (number) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            default -> String.valueOf(number);
        };
    }
}
