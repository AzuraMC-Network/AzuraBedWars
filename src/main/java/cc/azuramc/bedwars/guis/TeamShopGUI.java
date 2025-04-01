package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 团队商店GUI
 * 用于展示和处理各种团队升级选项
 */
public class TeamShopGUI extends CustomGUI {

    // 边框装饰颜色
    private static final int BORDER_GLASS_COLOR = 7; // 灰色

    // 升级项目名称
    private static final String SHARPENED_SWORDS = "磨刀石";
    private static final String REINFORCED_ARMOR = "精制护甲";
    private static final String MANIC_MINER = "疯狂矿工";
    private static final String MINING_FATIGUE_TRAP = "挖掘疲劳陷阱";
    private static final String HEALING_POOL = "治愈池";
    private static final String ALARM_TRAP = "警报陷阱";

    // 资源类型名称缓存
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();
    
    // 升级价格缓存（二维：升级类型 -> 等级 -> 价格）
    private static final Map<String, Map<Integer, Integer>> TIER_PRICES = new HashMap<>();
    
    // 静态初始化资源名称和价格
    static {
        // 初始化资源名称
        RESOURCE_NAMES.put(Material.DIAMOND, "钻石");
        
        // 初始化保护价格
        Map<Integer, Integer> armorPrices = new HashMap<>();
        armorPrices.put(0, 2);  // 护甲保护 I
        armorPrices.put(1, 4);  // 护甲保护 II
        armorPrices.put(2, 6);  // 护甲保护 III
        armorPrices.put(3, 8);  // 护甲保护 IV
        TIER_PRICES.put(REINFORCED_ARMOR, armorPrices);
        
        // 初始化疯狂矿工价格
        Map<Integer, Integer> minerPrices = new HashMap<>();
        minerPrices.put(0, 2);  // 急迫 I
        minerPrices.put(1, 4);  // 急迫 II
        TIER_PRICES.put(MANIC_MINER, minerPrices);
        
        // 初始化陷阱和其他升级价格
        Map<Integer, Integer> trapPrices = new HashMap<>();
        trapPrices.put(0, 2);  // 挖掘疲劳陷阱
        TIER_PRICES.put(MINING_FATIGUE_TRAP, trapPrices);
        
        Map<Integer, Integer> alarmPrices = new HashMap<>();
        alarmPrices.put(0, 2);  // 警报陷阱
        TIER_PRICES.put(ALARM_TRAP, alarmPrices);
        
        Map<Integer, Integer> healingPrices = new HashMap<>();
        healingPrices.put(0, 4);  // 治愈池
        TIER_PRICES.put(HEALING_POOL, healingPrices);
        
        Map<Integer, Integer> swordPrices = new HashMap<>();
        swordPrices.put(0, 4);  // 锋利
        TIER_PRICES.put(SHARPENED_SWORDS, swordPrices);
    }

    /**
     * 创建团队商店GUI
     * @param player 玩家
     * @param game 游戏实例
     */
    public TeamShopGUI(Player player, Game game) {
        super(player, "§8团队升级", 45);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        ModeType modeType = gamePlayer.getPlayerData().getModeType();
        GameTeam gameTeam = gamePlayer.getGameTeam();
        
        // 设置界面边框
        setupBorders();
        
        // 设置升级选项
        addSharpenedSwordsUpgrade(player, gamePlayer, game, modeType);
        addReinforcedArmorUpgrade(player, gamePlayer, game, modeType);
        addManicMinerUpgrade(player, gamePlayer, game, modeType);
        addMiningFatigueTrap(player, gamePlayer, game, modeType);
        addHealingPoolUpgrade(player, gamePlayer, game, modeType);
        addAlarmTrap(player, gamePlayer, game, modeType);
    }
    
    /**
     * 设置界面边框
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
        
        // 设置左右边框
        for (int row = 1; row < 5; row++) {
            int leftBorder = row * 9;
            int rightBorder = row * 9 + 8;
            setItem(leftBorder, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
            setItem(rightBorder, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
        
        // 设置底部边框
        for (int i = 36; i < 45; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
    }

    /**
     * 获取通用升级说明
     */
    private List<String> getUpgradeLore(String description, int price, ModeType modeType, boolean isUnlocked) {
        List<String> lore = new ArrayList<>();
        lore.add("§7" + description);
        lore.add("");
        
        if (isUnlocked) {
            lore.add("§a已购买");
        } else {
            lore.add("§7价格: §b" + formatPrice(price, modeType));
            lore.add("§e点击购买");
        }
        
        return lore;
    }
    
    /**
     * 获取分级升级说明 - 护甲专用
     */
    private List<String> getArmorUpgradeLore(int currentTier, ModeType modeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7队伍的所有护甲获得保护附魔！");
        lore.add("");
        
        // 添加4个等级的描述和价格
        for (int tier = 0; tier < 4; tier++) {
            String tierColor;
            if (tier < currentTier) {
                tierColor = "§a"; // 已购买的等级
            } else if (tier == currentTier) {
                tierColor = "§e"; // 当前可购买的等级
            } else {
                tierColor = "§7"; // 未解锁的等级
            }
            
            int displayTier = tier + 1;
            int price = TIER_PRICES.get(REINFORCED_ARMOR).get(tier);
            
            lore.add(tierColor + "等级 " + displayTier + "：保护 " + getRomanNumeral(displayTier) + 
                    "，§b" + formatPrice(price, modeType));
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
    private List<String> getMinerUpgradeLore(int currentTier, ModeType modeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7队伍的所有成员获得急迫效果！");
        lore.add("");
        
        // 添加2个等级的描述和价格
        for (int tier = 0; tier < 2; tier++) {
            String tierColor;
            if (tier < currentTier) {
                tierColor = "§a"; // 已购买的等级
            } else if (tier == currentTier) {
                tierColor = "§e"; // 当前可购买的等级
            } else {
                tierColor = "§7"; // 未解锁的等级
            }
            
            int displayTier = tier + 1;
            int price = TIER_PRICES.get(MANIC_MINER).get(tier);
            
            lore.add(tierColor + "等级 " + displayTier + "：急迫 " + getRomanNumeral(displayTier) + 
                    "，§b" + formatPrice(price, modeType));
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
     * 获取分级升级说明 - 锋利专用
     */
    private List<String> getSwordUpgradeLore(boolean isUnlocked, ModeType modeType) {
        List<String> lore = new ArrayList<>();
        lore.add("§7队伍的所有剑获得锋利I附魔！");
        lore.add("");
        
        // 显示等级和价格
        String tierColor = isUnlocked ? "§a" : "§e";
        int price = TIER_PRICES.get(SHARPENED_SWORDS).get(0);
        
        lore.add(tierColor + "等级 1：锋利 I，§b" + formatPrice(price, modeType));
        lore.add("");
        
        if (isUnlocked) {
            lore.add("§a已购买");
        } else {
            lore.add("§e点击购买");
        }
        
        return lore;
    }

    /**
     * 添加锋利升级选项
     */
    private void addSharpenedSwordsUpgrade(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        
        if (!gameTeam.isSharpenedSwords()) {
            // 未升级状态
            int price = TIER_PRICES.get(SHARPENED_SWORDS).get(0);
            
            setItem(11, new ItemBuilderUtil()
                    .setType(Material.IRON_SWORD)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + SHARPENED_SWORDS)
                    .setLores(getSwordUpgradeLore(false, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setSharpenedSwords(true);
                        new TeamShopGUI(player, game).open();
                        
                        // 为团队所有玩家的剑添加锋利附魔
                        for (GamePlayer teamPlayer : gameTeam.getAlivePlayers()) {
                            Player p = teamPlayer.getPlayer();
                            
                            for (int i = 0; i < p.getInventory().getContents().length; i++) {
                                ItemStack item = p.getInventory().getContents()[i];
                                if (item != null && item.getType().toString().endsWith("_SWORD")) {
                                    item.addEnchantment(EnchantmentUtil.DAMAGE_ALL(), 1);
                                }
                            }
                        }
                    }, false));
        } else {
            // 已升级状态
            setItem(11, new ItemBuilderUtil()
                    .setType(Material.IRON_SWORD)
                    .addEnchant(EnchantmentUtil.DAMAGE_ALL(), 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + SHARPENED_SWORDS)
                    .setLores(getSwordUpgradeLore(true, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 添加保护升级选项
     */
    private void addReinforcedArmorUpgrade(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getReinforcedArmor();
        
        if (currentLevel < 4) {
            // 未达到最高级
            int price = TIER_PRICES.get(REINFORCED_ARMOR).get(currentLevel);
            int nextLevel = currentLevel + 1;
            
            setItem(12, new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_CHESTPLATE())
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + REINFORCED_ARMOR)
                    .setLores(getArmorUpgradeLore(currentLevel, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setReinforcedArmor(nextLevel);
                        new TeamShopGUI(player, game).open();
                        
                        // 为团队所有玩家的护甲添加保护附魔
                        for (GamePlayer teamPlayer : gameTeam.getAlivePlayers()) {
                            Player p = teamPlayer.getPlayer();
                            
                            for (int i = 0; i < p.getInventory().getArmorContents().length; i++) {
                                ItemStack armor = p.getInventory().getArmorContents()[i];
                                if (armor != null) {
                                    armor.addEnchantment(EnchantmentUtil.PROTECTION_ENVIRONMENTAL(), nextLevel);
                                }
                            }
                        }
                    }, false));
        } else {
            // 已达到最高级
            setItem(12, new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_CHESTPLATE())
                    .addEnchant(EnchantmentUtil.PROTECTION_ENVIRONMENTAL(), 4)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + REINFORCED_ARMOR)
                    .setLores(getArmorUpgradeLore(currentLevel, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 添加疯狂矿工升级选项
     */
    private void addManicMinerUpgrade(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        int currentLevel = gameTeam.getManicMiner();
        
        if (currentLevel < 2) {
            // 未达到最高级
            int price = TIER_PRICES.get(MANIC_MINER).get(currentLevel);
            int nextLevel = currentLevel + 1;
            
            setItem(13, new ItemBuilderUtil()
                    .setType(MaterialUtil.GOLDEN_PICKAXE())
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + MANIC_MINER)
                    .setLores(getMinerUpgradeLore(currentLevel, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setManicMiner(nextLevel);
                        new TeamShopGUI(player, game).open();
                    }, false));
        } else {
            // 已达到最高级
            setItem(13, new ItemBuilderUtil()
                    .setType(MaterialUtil.GOLDEN_PICKAXE())
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 2)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + MANIC_MINER)
                    .setLores(getMinerUpgradeLore(currentLevel, modeType))
                    .getItem(), 
                    new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 添加挖掘疲劳陷阱升级选项
     */
    private void addMiningFatigueTrap(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        
        if (!gameTeam.isMiner()) {
            // 未升级状态
            int price = TIER_PRICES.get(MINING_FATIGUE_TRAP).get(0);
            
            setItem(14, new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_PICKAXE())
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + MINING_FATIGUE_TRAP)
                    .setLores(
                        "§7下一个进入基地的敌人将获得",
                        "§7持续10秒的挖掘疲劳效果！",
                        "",
                        "§7价格: §b" + formatPrice(price, modeType),
                        "§e点击购买"
                    )
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setMiner(true);
                        new TeamShopGUI(player, game).open();
                    }, false));
        } else {
            // 已升级状态
            setItem(14, new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_PICKAXE())
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName("§a" + MINING_FATIGUE_TRAP)
                    .setLores(
                        "§7下一个进入基地的敌人将获得",
                        "§7持续10秒的挖掘疲劳效果！",
                        "",
                        "§a已激活"
                    )
                    .getItem(), 
                    new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 添加治愈池升级选项
     */
    private void addHealingPoolUpgrade(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        
        if (!gameTeam.isHealPool()) {
            // 未升级状态
            int price = TIER_PRICES.get(HEALING_POOL).get(0);
            
            setItem(15, new ItemBuilderUtil()
                    .setType(MaterialUtil.BEACON())
                    .setDisplayName("§a" + HEALING_POOL)
                    .setLores(
                        "§7基地附近的队伍成员获得",
                        "§7生命恢复效果",
                        "",
                        "§7价格: §b" + formatPrice(price, modeType),
                        "§e点击购买"
                    )
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setHealPool(true);
                        new TeamShopGUI(player, game).open();
                    }, false));
        } else {
            // 已升级状态
            setItem(15, new ItemBuilderUtil()
                    .setType(MaterialUtil.BEACON())
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
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
     * 添加警报陷阱升级选项
     */
    private void addAlarmTrap(Player player, GamePlayer gamePlayer, Game game, ModeType modeType) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        
        if (!gameTeam.isAlarmTrap()) {
            // 未升级状态
            int price = TIER_PRICES.get(ALARM_TRAP).get(0);
            
            setItem(16, new ItemBuilderUtil()
                    .setType(Material.REDSTONE_TORCH)
                    .setDisplayName("§a" + ALARM_TRAP)
                    .setLores(
                        "§7下一个进入基地的敌人会触发警报",
                        "§7提醒并被标记10秒，显示准确位置",
                        "",
                        "§7价格: §b" + formatPrice(price, modeType),
                        "§e点击购买"
                    )
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (!processPayment(player, price, modeType)) {
                            return;
                        }
                        
                        gameTeam.setAlarmTrap(true);
                        new TeamShopGUI(player, game).open();
                    }, false));
        } else {
            // 已升级状态
            setItem(16, new ItemBuilderUtil()
                    .setType(Material.REDSTONE_TORCH)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName("§a" + ALARM_TRAP)
                    .setLores(
                        "§7下一个进入基地的敌人会触发警报",
                        "§7提醒并被标记10秒，显示准确位置",
                        "",
                        "§a已激活"
                    )
                    .getItem(), 
                    new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 处理支付
     * @param player 玩家
     * @param price 价格
     * @param modeType 游戏模式
     * @return 是否支付成功
     */
    private boolean processPayment(Player player, int price, ModeType modeType) {
        if (modeType == ModeType.DEFAULT) {
            // 默认模式：支付钻石
            return processItemPayment(player, Material.DIAMOND, price);
        } else {
            // 经验模式：支付经验
            return processExperiencePayment(player, price * 100);
        }
    }
    
    /**
     * 处理物品支付
     * @param player 玩家
     * @param material 物品类型
     * @param amount 数量
     * @return 是否支付成功
     */
    private boolean processItemPayment(Player player, Material material, int amount) {
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
            SoundUtil.playEndermanTeleportSound(player);
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
        
        SoundUtil.playItemPickupSound(player);
        return true;
    }
    
    /**
     * 处理经验支付
     * @param player 玩家
     * @param xpLevel 经验等级
     * @return 是否支付成功
     */
    private boolean processExperiencePayment(Player player, int xpLevel) {
        if (player.getLevel() < xpLevel) {
            SoundUtil.playEndermanTeleportSound(player);
            player.sendMessage("§c没有足够资源购买！");
            return false;
        }
        
        player.setLevel(player.getLevel() - xpLevel);
        SoundUtil.playItemPickupSound(player);
        return true;
    }
    
    /**
     * 格式化价格显示
     * @param price 价格
     * @param modeType 游戏模式
     * @return 格式化后的价格字符串
     */
    private String formatPrice(int price, ModeType modeType) {
        if (modeType == ModeType.DEFAULT) {
            return price + " " + RESOURCE_NAMES.get(Material.DIAMOND);
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
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number);
        }
    }
}
