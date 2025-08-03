package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.trap.TrapManager;
import cc.azuramc.bedwars.game.trap.TrapState;
import cc.azuramc.bedwars.game.trap.TrapType;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 团队商店GUI
 * 用于展示和处理各种团队升级选项
 *
 * @author an5w1r@163.com
 */
public class TeamShopGUI extends CustomGUI {

    /** 灰色 */
    private static final int BORDER_GLASS_COLOR = 7;

    private static final String SHARPENED_SWORDS = "磨刀石";
    private static final String REINFORCED_ARMOR = "精制护甲";
    private static final String MANIC_MINER = "疯狂矿工";
    private static final String RESOURCE_FURNACE = "铁锻炉";
    private static final String HEALING_POOL = "治愈池";
    private static final String FALLING_PROTECTION = "缓冲靴子";

    /** 资源类型名称缓存 */
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();

    /** 升级价格缓存（二维：升级类型 -> 等级 -> 价格） */
    private static final Map<String, Map<Integer, Integer>> TIER_PRICES = new HashMap<>();

    // 静态初始化资源名称和价格
    static {
        // 初始化资源名称
        RESOURCE_NAMES.put(XMaterial.DIAMOND.get(), "钻石");

        // 锋利
        Map<Integer, Integer> swordPrices = new HashMap<>();
        swordPrices.put(0, 8);
        TIER_PRICES.put(SHARPENED_SWORDS, swordPrices);

        // 初始化保护价格
        Map<Integer, Integer> armorPrices = new HashMap<>();
        // 护甲保护 I
        armorPrices.put(0, 5);
        // 护甲保护 II
        armorPrices.put(1, 10);
        // 护甲保护 III
        armorPrices.put(2, 20);
        // 护甲保护 IV
        armorPrices.put(3, 30);
        TIER_PRICES.put(REINFORCED_ARMOR, armorPrices);

        // 初始化资源炉价格
        Map<Integer, Integer> resourceFurnacePrices = new HashMap<>();
        // I
        resourceFurnacePrices.put(0, 4);
        // II
        resourceFurnacePrices.put(1, 8);
        // III
        resourceFurnacePrices.put(2, 12);
        // IV
        resourceFurnacePrices.put(3, 16);
        TIER_PRICES.put(RESOURCE_FURNACE, resourceFurnacePrices);

        // 治愈池
        Map<Integer, Integer> healingPrices = new HashMap<>();
        healingPrices.put(0, 3);
        TIER_PRICES.put(HEALING_POOL, healingPrices);

        // 初始化疯狂矿工价格
        Map<Integer, Integer> minerPrices = new HashMap<>();
        // 急迫 I
        minerPrices.put(0, 4);
        // 急迫 II
        minerPrices.put(1, 6);
        TIER_PRICES.put(MANIC_MINER, minerPrices);

        // 初始化摔落保护价格
        Map<Integer, Integer> fallingProtectionPrices = new HashMap<>();
        // 摔落保护 I
        fallingProtectionPrices.put(0, 2);
        // 摔落保护 II
        fallingProtectionPrices.put(1, 4);
        TIER_PRICES.put(FALLING_PROTECTION, fallingProtectionPrices);
    }

    /**
     * 创建团队商店GUI
     * @param gamePlayer 玩家
     * @param gameManager 游戏实例
     */
    public TeamShopGUI(GamePlayer gamePlayer, GameManager gameManager) {
        super(gamePlayer, "§8团队升级", 54);

        GameModeType gameModeType = gamePlayer.getPlayerData().getMode();

        // 设置界面边框
        setupBorders();

        // 设置升级选项
        addSharpenedSwordsUpgrade(gamePlayer, gameManager, gameModeType);
        addReinforcedArmorUpgrade(gamePlayer, gameManager, gameModeType);
        addManicMinerUpgrade(gamePlayer, gameManager, gameModeType);
        addResourceFurnaceUpgrade(gamePlayer, gameManager, gameModeType);
        addHealingPoolUpgrade(gamePlayer, gameManager, gameModeType);
        addFallingProtectionUpgrade(gamePlayer, gameManager, gameModeType);

        // 陷阱选项
        addBlindnessTrap(gamePlayer, gameManager, gameModeType);
        addFightBackTrap(gamePlayer, gameManager, gameModeType);
        addAlarmTrap(gamePlayer, gameManager, gameModeType);
        addMinerTrap(gamePlayer, gameManager, gameModeType);

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
     * 添加锋利升级选项
     */
    private void addSharpenedSwordsUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (!gameTeam.isHasSharpnessUpgrade()) {
            // 未升级状态
            int price = TIER_PRICES.get(SHARPENED_SWORDS).get(0);

            setItem(10, new ItemBuilder()
                            .setType(XMaterial.IRON_SWORD.get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + SHARPENED_SWORDS)
                            .setLores(getSwordUpgradeLore(false, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setHasSharpnessUpgrade(true);
                        new TeamShopGUI(gamePlayer, gameManager).open();

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
                    }, false));
        } else {
            // 已升级状态
            setItem(10, new ItemBuilder()
                            .setType(XMaterial.IRON_SWORD.get())
                            .addEnchant(XEnchantment.SHARPNESS.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + SHARPENED_SWORDS)
                            .setLores(getSwordUpgradeLore(true, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加保护升级选项
     */
    private void addReinforcedArmorUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getProtectionUpgrade();

        if (currentLevel < 4) {
            // 未达到最高级
            int price = TIER_PRICES.get(REINFORCED_ARMOR).get(currentLevel);
            int nextLevel = currentLevel + 1;

            setItem(11, new ItemBuilder()
                            .setType(XMaterial.IRON_CHESTPLATE.get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + REINFORCED_ARMOR)
                            .setLores(getArmorUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setProtectionUpgrade(nextLevel);
                        new TeamShopGUI(gamePlayer, gameManager).open();

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
                    }, false));
        } else {
            // 已达到最高级
            setItem(11, new ItemBuilder()
                            .setType(XMaterial.IRON_CHESTPLATE.get())
                            .addEnchant(XEnchantment.PROTECTION.get(), 4)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + REINFORCED_ARMOR)
                            .setLores(getArmorUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加疯狂矿工升级选项
     */
    private void addManicMinerUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getMagicMinerUpgrade();

        if (currentLevel < 2) {
            // 未达到最高级
            int price = TIER_PRICES.get(MANIC_MINER).get(currentLevel);
            int nextLevel = currentLevel + 1;

            setItem(12, new ItemBuilder()
                            .setType(XMaterial.GOLDEN_PICKAXE.get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + MANIC_MINER)
                            .setLores(getMinerUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setMagicMinerUpgrade(nextLevel);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else {
            // 已达到最高级
            setItem(12, new ItemBuilder()
                            .setType(XMaterial.GOLDEN_PICKAXE.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 2)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + MANIC_MINER)
                            .setLores(getMinerUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加资源炉升级选项
     */
    private void addResourceFurnaceUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getResourceFurnaceUpgrade();

        if (currentLevel < 4) {
            // 未达到最高级
            int price = TIER_PRICES.get(RESOURCE_FURNACE).get(currentLevel);
            int nextLevel = currentLevel + 1;

            setItem(19, new ItemBuilder()
                            .setType(XMaterial.FURNACE.get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + RESOURCE_FURNACE)
                            .setLores(getResourceFurnaceUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setResourceFurnaceUpgrade(nextLevel);
                        LoggerUtil.debug("资源炉升级到等级 " + nextLevel + "，当前价格: " + price);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else {
            // 已达到最高级
            setItem(19, new ItemBuilder()
                            .setType(XMaterial.FURNACE.get())
                            .addEnchant(XEnchantment.PROTECTION.get(), 4)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + RESOURCE_FURNACE)
                            .setLores(getResourceFurnaceUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加治愈池升级选项
     */
    private void addHealingPoolUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (!gameTeam.isHasHealPoolUpgrade()) {
            // 未升级状态
            int price = TIER_PRICES.get(HEALING_POOL).get(0);

            setItem(20, new ItemBuilder()
                            .setType(XMaterial.BEACON.get())
                            .setDisplayName("§a" + HEALING_POOL)
                            .setLores(
                                    "§7基地附近的队伍成员将获得生命恢复效果！",
                                    "",
                                    "§7价格: §b" + formatPrice(price, gameModeType),
                                    "§e点击购买"
                            )
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setHasHealPoolUpgrade(true);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else {
            // 已升级状态
            setItem(20, new ItemBuilder()
                            .setType(XMaterial.BEACON.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .setDisplayName("§a" + HEALING_POOL)
                            .setLores(
                                    "§7基地附近的队伍成员获得",
                                    "§7生命恢复效果",
                                    "",
                                    "§a已激活"
                            )
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加摔落保护升级选项
     */
    private void addFallingProtectionUpgrade(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getFallingProtectionUpgrade();

        if (currentLevel < 2) {
            // 未达到最高级
            int price = TIER_PRICES.get(FALLING_PROTECTION).get(currentLevel);
            int nextLevel = currentLevel + 1;

            setItem(21, new ItemBuilder()
                            .setType(XMaterial.DIAMOND_BOOTS.get())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + FALLING_PROTECTION)
                            .setLores(getFallingProtectionUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        gameTeam.setFallingProtectionUpgrade(nextLevel);
                        new TeamShopGUI(gamePlayer, gameManager).open();

                        // 为团队所有玩家的护甲添加摔落保护附魔
                        gameTeam.getAlivePlayers().forEach(gamePlayers -> {
                            ItemStack boots = gamePlayers.getPlayer().getInventory().getArmorContents()[0];
                            if (boots != null) {
                                boots.addEnchantment(XEnchantment.FEATHER_FALLING.get(), gameTeam.getFallingProtectionUpgrade());
                                gamePlayers.updateInventory();
                            }
                        });
                    }, false));
        } else {
            // 已达到最高级
            setItem(21, new ItemBuilder()
                            .setType(XMaterial.DIAMOND_BOOTS.get())
                            .addEnchant(XEnchantment.PROTECTION.get(), 4)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + FALLING_PROTECTION)
                            .setLores(getFallingProtectionUpgradeLore(currentLevel, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }


    /**
     * 添加失明陷阱升级选项
     */
    private void addBlindnessTrap(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        int price = trapManager.getCurrentTrapPrice();

        if (!trapManager.isTrapActive(TrapType.BLINDNESS)) {
            // 未升级状态
            setItem(14, new ItemBuilder()
                            .setType(getBlindnessTrapMaterial())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.BLINDNESS.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.INACTIVE, getBlindnessTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        trapManager.activateTrap(TrapType.BLINDNESS);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else if (!trapManager.isReachedActiveLimit()) {
            // 已升级状态
            setItem(14, new ItemBuilder()
                            .setType(XMaterial.TRIPWIRE_HOOK.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.BLINDNESS.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.ACTIVE, getBlindnessTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        } else {
            setItem(14, new ItemBuilder()
                            .setType(XMaterial.TRIPWIRE_HOOK.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.BLINDNESS.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.FULLED, getBlindnessTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加挖掘疲劳陷阱升级选项
     */
    private void addFightBackTrap(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        int price = trapManager.getCurrentTrapPrice();

        if (!trapManager.isTrapActive(TrapType.FIGHT_BACK)) {
            // 未升级状态

            setItem(15, new ItemBuilder()
                            .setType(getFightbackTrapMaterial())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.FIGHT_BACK.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.INACTIVE, getFightbackTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        trapManager.activateTrap(TrapType.FIGHT_BACK);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else if (trapManager.isReachedActiveLimit()) {
            // 已升级状态
            setItem(15, new ItemBuilder()
                            .setType(XMaterial.FEATHER.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.FIGHT_BACK.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.ACTIVE, getFightbackTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        } else {
            setItem(15, new ItemBuilder()
                            .setType(XMaterial.FEATHER.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.FIGHT_BACK.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.FULLED, getFightbackTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加警报陷阱升级选项
     */
    private void addAlarmTrap(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        int price = trapManager.getCurrentTrapPrice();

        if (!trapManager.isTrapActive(TrapType.ALARM)) {
            // 未升级状态

            setItem(16, new ItemBuilder()
                            .setType(getAlarmTrapMaterial())
                            .setDisplayName("§a" + TrapType.ALARM.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.INACTIVE, getAlarmTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        trapManager.activateTrap(TrapType.ALARM);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else if (trapManager.isReachedActiveLimit()) {
            // 已升级状态
            setItem(16, new ItemBuilder()
                            .setType(XMaterial.REDSTONE_TORCH.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .setDisplayName("§a" + TrapType.ALARM.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.ACTIVE, getAlarmTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        } else {
            setItem(16, new ItemBuilder()
                            .setType(XMaterial.REDSTONE_TORCH.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .setDisplayName("§a" + TrapType.ALARM.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.FULLED, getAlarmTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 添加挖掘疲劳陷阱升级选项
     */
    private void addMinerTrap(GamePlayer gamePlayer, GameManager gameManager, GameModeType gameModeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        int price = trapManager.getCurrentTrapPrice();


        if (!trapManager.isTrapActive(TrapType.MINER)) {
            // 未升级状态
            setItem(23, new ItemBuilder()
                            .setType(getMinerTrapMaterial())
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.MINER.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.INACTIVE, getMinerTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (!processPayment(gamePlayer, price, gameModeType)) {
                            return;
                        }

                        trapManager.activateTrap(TrapType.MINER);
                        new TeamShopGUI(gamePlayer, gameManager).open();
                    }, false));
        } else if (trapManager.isReachedActiveLimit()) {
            // 已升级状态
            setItem(23, new ItemBuilder()
                            .setType(XMaterial.IRON_PICKAXE.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.MINER.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.ACTIVE, getMinerTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        } else {
            setItem(23, new ItemBuilder()
                            .setType(XMaterial.IRON_PICKAXE.get())
                            .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .setDisplayName("§a" + TrapType.MINER.getDisplayName())
                            .setLores(getTrapItemLore(TrapState.FULLED, getMinerTrapLore(), price, gameModeType))
                            .getItem(),
                    new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 设置陷阱列表
     */
    private void setupTrapList(GamePlayer gamePlayer) {
        TrapManager trapManager = gamePlayer.getGameTeam().getTrapManager();
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

        if (trapManager.getActiveTrapCount() >= 1) {
            for (int j = 39; j < trapManager.getActiveTrapCount() + 39; j++) {
                setItem(j, new ItemBuilder()
                                .setType(getTrapMaterialByTrapType(trapManager.getActiveTraps().get(j - 39)))
                                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .setDisplayName("§a陷阱#" + (j - 38) + ": " + trapManager.getActiveTraps().get(j - 39).getDisplayName())
                                .setLores(
                                        "§7第" + getChineseNumber(j - 38) + "个敌人进入己方基地后",
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
        }
    }

    /**
     * 获取分级升级说明 - 锋利专用
     */
    private List<String> getSwordUpgradeLore(boolean isUnlocked, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7你方所有成员的剑将永久获得锋利I附魔！");
        lore.add("");

        // 显示等级和价格
        String tierColor = isUnlocked ? "§a" : "§e";
        int price = TIER_PRICES.get(SHARPENED_SWORDS).get(0);

        lore.add(tierColor + "等级 1：锋利 I，§b" + formatPrice(price, gameModeType));
        lore.add("");

        if (isUnlocked) {
            lore.add("§a已购买");
        } else {
            lore.add("§e点击购买");
        }

        return lore;
    }

    /**
     * 获取分级升级说明 - 护甲专用
     */
    private List<String> getArmorUpgradeLore(int currentTier, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7己方所有成员的盔甲将获得永久保护附魔！");
        lore.add("");

        // 添加4个等级的描述和价格
        for (int tier = 0; tier < 4; tier++) {
            String tierColor;
            if (tier < currentTier) {
                // 已购买的等级
                tierColor = "§a";
            } else if (tier == currentTier) {
                // 当前可购买的等级
                tierColor = "§e";
            } else {
                // 未解锁的等级
                tierColor = "§7";
            }

            int displayTier = tier + 1;
            int price = TIER_PRICES.get(REINFORCED_ARMOR).get(tier);

            lore.add(tierColor + "等级 " + displayTier + "：保护 " + getRomanNumeral(displayTier) +
                    "，§b" + formatPrice(price, gameModeType));
        }

        lore.add("");

        if (currentTier < 4) {
            lore.add("§e点击升级到保护 " + getRomanNumeral(currentTier + 1));
        } else {
            lore.add("§a已达到最高等级");
        }

        return lore;
    }

    /**
     * 获取分级升级说明 - 疯狂矿工专用
     */
    private List<String> getMinerUpgradeLore(int currentTier, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7己方所有成员获得急迫效果。");
        lore.add("");

        // 添加2个等级的描述和价格
        for (int tier = 0; tier < 2; tier++) {
            String tierColor;
            if (tier < currentTier) {
                // 已购买的等级
                tierColor = "§a";
            } else if (tier == currentTier) {
                // 当前可购买的等级
                tierColor = "§e";
            } else {
                // 未解锁的等级
                tierColor = "§7";
            }

            int displayTier = tier + 1;
            int price = TIER_PRICES.get(MANIC_MINER).get(tier);

            lore.add(tierColor + "等级 " + displayTier + "：急迫 " + getRomanNumeral(displayTier) +
                    "，§b" + formatPrice(price, gameModeType));
        }

        lore.add("");

        if (currentTier < 2) {
            lore.add("§e点击升级到急迫 " + getRomanNumeral(currentTier + 1));
        } else {
            lore.add("§a已达到最高等级");
        }

        return lore;
    }

    /**
     * 获取分级升级说明 - 资源炉专用
     */
    private List<String> getResourceFurnaceUpgradeLore(int currentTier, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7升级你岛屿资源池的生成速度和和最大容量.");
        lore.add("");

        // 添加4个等级的描述和价格
        for (int tier = 0; tier < 4; tier++) {
            String tierColor;
            if (tier < currentTier) {
                // 已购买的等级
                tierColor = "§a";
            } else if (tier == currentTier) {
                // 当前可购买的等级
                tierColor = "§e";
            } else {
                // 未解锁的等级
                tierColor = "§7";
            }

            int displayTier = tier + 1;
            int price = TIER_PRICES.get(RESOURCE_FURNACE).get(tier);

            String effectDescription = switch (displayTier) {
                case 1 -> "+50%资源";
                case 2 -> "+100%资源";
                case 3 -> "生成绿宝石";
                case 4 -> "+200%资源";
                default -> "";
            };

            lore.add(tierColor + "等级 " + displayTier + "： " + effectDescription +
                    "，§b" + formatPrice(price, gameModeType));
        }

        lore.add("");

        if (currentTier < 4) {
            lore.add("§e点击升级到等级 " + getRomanNumeral(currentTier + 1));
        } else {
            lore.add("§a已达到最高等级");
        }

        return lore;
    }

    /**
     * 获取分级升级说明 - 资源炉专用
     */
    private List<String> getFallingProtectionUpgradeLore(int currentTier, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7你队伍的靴子获得了永久摔落保护！");
        lore.add("");

        // 添加2个等级的描述和价格
        for (int tier = 0; tier < 2; tier++) {
            String tierColor;
            if (tier < currentTier) {
                // 已购买的等级
                tierColor = "§a";
            } else if (tier == currentTier) {
                // 当前可购买的等级
                tierColor = "§e";
            } else {
                // 未解锁的等级
                tierColor = "§7";
            }

            int displayTier = tier + 1;
            int price = TIER_PRICES.get(FALLING_PROTECTION).get(tier);

            lore.add(tierColor + "等级 " + displayTier + "：缓冲靴子 " + getRomanNumeral(displayTier) +
                    "，§b" + formatPrice(price, gameModeType));
        }

        lore.add("");

        if (currentTier < 2) {
            lore.add("§e点击升级到缓冲靴子 " + getRomanNumeral(currentTier + 1));
        } else {
            lore.add("§a已达到最高等级");
        }

        return lore;
    }

    /**
     * 获取陷阱物品Lore
     * @param trapLore 指定陷阱的lore
     * @param price 价格
     * @param gameModeType 游戏模式
     * @return 完整拼接好的lore
     */
    private List<String> getTrapItemLore(TrapState trapState, List<String> trapLore, int price, GameModeType gameModeType) {
        List<String> lores = new ArrayList<>(trapLore);
        switch (trapState) {
            case INACTIVE:
                lores.add("");
                lores.add("§7价格: §b" + formatPrice(price, gameModeType));
                lores.add("");
                lores.add("§e点击购买");
                return lores;
            case ACTIVE:
                lores.add("");
                lores.add("§7价格: §b" + formatPrice(price, gameModeType));
                lores.add("");
                lores.add("§a已激活");
                return lores;
            case FULLED:
                lores.add("");
                lores.add("§7价格: §b" + formatPrice(price, gameModeType));
                lores.add("");
                lores.add("§c陷阱已满");
                return lores;
            default:
                return lores;
        }
    }

    /**
     * 获取陷阱图标材质 - 失明陷阱专用
     */
    private Material getBlindnessTrapMaterial() {
        return XMaterial.TRIPWIRE_HOOK.get();
    }

    /**
     * 获取陷阱图标材质 - 反击陷阱专用
     */
    private Material getFightbackTrapMaterial() {
        return XMaterial.FEATHER.get();
    }

    /**
     * 获取陷阱图标材质 - 警报陷阱专用
     */
    private Material getAlarmTrapMaterial() {
        return XMaterial.REDSTONE_TORCH.get();
    }

    /**
     * 获取陷阱图标材质 - 挖掘疲劳陷阱专用
     */
    private Material getMinerTrapMaterial() {
        return XMaterial.IRON_PICKAXE.get();
    }

    /**
     * 获取陷阱说明 - 失明陷阱专用
     */
    private List<String> getBlindnessTrapLore() {
        return List.of(
                "§7造成失明与缓慢效果，持续8秒。"
        );
    }

    /**
     * 获取陷阱说明 - 反击陷阱专用
     */
    private List<String> getFightbackTrapLore() {
        return List.of(
                "§7赋予基地附近的队友速度 II 与跳跃提升 II",
                "§7效果，持续15秒。"
        );
    }

    /**
     * 获取陷阱说明 - 警报陷阱专用
     */
    private List<String> getAlarmTrapLore() {
        return List.of(
                "§7显示隐身的玩家，",
                "§7及其名称与队伍名。"
        );
    }

    /**
     * 获取陷阱说明 - 挖掘疲劳陷阱专用
     */
    private List<String> getMinerTrapLore() {
        return List.of(
                "§7造成挖掘疲劳效果，持续8秒。"
        );
    }

    /**
     * 根据陷阱类型获取对应的图标材质
     * @param trapType 陷阱类型
     * @return 对应的陷阱图标材质
     */
    private Material getTrapMaterialByTrapType(TrapType trapType) {
        return switch (trapType) {
            case BLINDNESS -> getBlindnessTrapMaterial();
            case FIGHT_BACK -> getFightbackTrapMaterial();
            case ALARM -> getAlarmTrapMaterial();
            case MINER -> getMinerTrapMaterial();
        };
    }

    /**
     * 根据陷阱类型获取对应的Lore
     * @param trapType 陷阱类型
     * @return 对应的陷阱Lore
     */
    private List<String> getTrapLoreByTrapType(TrapType trapType) {
        return switch (trapType) {
            case BLINDNESS -> getBlindnessTrapLore();
            case FIGHT_BACK -> getFightbackTrapLore();
            case ALARM -> getAlarmTrapLore();
            case MINER -> getMinerTrapLore();
        };
    }

    /**
     * 处理支付
     * @param gamePlayer 游戏玩家
     * @param price 价格
     * @param gameModeType 游戏模式
     * @return 是否支付成功
     */
    private boolean processPayment(GamePlayer gamePlayer, int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            // 默认模式：支付钻石
            return processItemPayment(gamePlayer, XMaterial.DIAMOND.get(), price);
        } else {
            // 经验模式：支付经验
            return processExperiencePayment(gamePlayer, price * 100);
        }
    }

    /**
     * 处理物品支付
     * @param gamePlayer 玩家
     * @param material 物品类型
     * @param amount 数量
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
     * @param gamePlayer 游戏玩家
     * @param xpLevel 经验等级
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
     * @param price 价格
     * @param gameModeType 游戏模式
     * @return 格式化后的价格字符串
     */
    private String formatPrice(int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return price + " " + RESOURCE_NAMES.get(XMaterial.DIAMOND.get());
        } else {
            return (price * 100) + "级";
        }
    }

    /**
     * 获取罗马数字
     * @param number 数字
     * @return 罗马数字表示
     */
    private String getRomanNumeral(int number) {
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
