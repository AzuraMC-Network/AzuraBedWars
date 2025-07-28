package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class KillRewardHandler {

    public static void processKillReward(PlayerDeathEvent event, GamePlayer gamePlayer, GamePlayer gameKiller) {
        if (gamePlayer == null) {
            return;
        }

        if (gameKiller.getGameModeType() == GameModeType.EXPERIENCE) {
            // 1. 击杀者是经验模式
            if (gamePlayer.getGameModeType() == GameModeType.EXPERIENCE) {
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player and killer all the 'EXPERIENCE' mode'");
                // 1.1 被击杀者也是经验模式，直接给经验，无需转换
                // 从experienceSources直接给予经验
                // FIXME: 转换未考虑消费问题
//                convertExperienceSourcesToExp(gamePlayer, gameKiller);
            } else {
                // 1.2 被击杀者是default模式，需要将物品转换为经验
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player is 'EXPERIENCE' mode , killer is 'DEFAULT' mode");
//                gameKiller.getPlayer().giveExpLevels(getPlayerRewardExp(gamePlayer.getPlayer()));
            }
        } else {
            // 2. 击杀者是default模式
            if (gamePlayer.getGameModeType() == GameModeType.EXPERIENCE) {
                // 2.1 被击杀者是经验模式，需要将经验转换为物品
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player is 'DEFAULT' mode , killer is 'EXPERIENCE' mode");
//                convertExperienceSourcesToItems(gamePlayer, gameKiller, event);
            } else {
                // 2.2 被击杀者是default模式，直接转移物品
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player and killer all the 'DEFAULT' mode'");
//                transferItemsToKiller(gamePlayer.getPlayer(), gameKiller.getPlayer(), event);
            }
        }

        // 清除经验来源map
        gamePlayer.getExperienceSources().clear();
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | expMapCleared");
    }

    /**
     * 获取玩家死亡时身上的资源（经典模式）
     *
     * @param player 要获取的目标玩家（死亡玩家）
     */
    private static int getPlayerRewardExp(Player player) {
        // 处理物品类型资源
        Map<Material, Integer> items = new HashMap<>();
        items.put(XMaterial.IRON_INGOT.get(), 0);
        items.put(XMaterial.GOLD_INGOT.get(), 0);
        items.put(XMaterial.DIAMOND.get(), 0);
        items.put(XMaterial.EMERALD.get(), 0);
        // 遍历背包 得到各类物品资源总数 存在 items 中
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }

            Material itemType = item.getType();
            if (items.containsKey(itemType)) {
                items.put(itemType, items.get(itemType) + item.getAmount());
            }
        }

        // 处理经验资源
        int oldExp = player.getLevel();
        int ironExp = items.get(XMaterial.IRON_INGOT.get());
        int goldExp = items.get(XMaterial.GOLD_INGOT.get()) * 3;
        int diamondExp = items.get(XMaterial.DIAMOND.get()) * 40;
        int emeraldExp = items.get(XMaterial.EMERALD.get()) * 80;

        // 目标玩家的全部身家 :w:
        return oldExp + ironExp + goldExp + diamondExp + emeraldExp;
    }

    /**
     * 将被击杀者的经验来源转换为经验值给予击杀者
     *
     * @param gamePlayer 被击杀的游戏玩家
     * @param gameKiller 击杀者
     */
    private static void convertExperienceSourcesToExp(GamePlayer gamePlayer, GamePlayer gameKiller) {
        int totalExp = 0;
        Map<String, Integer> expSources = gamePlayer.getExperienceSources();

        // 按照不同资源类型计算总经验值
        for (Map.Entry<String, Integer> entry : expSources.entrySet()) {
            String resourceType = entry.getKey();
            int amount = entry.getValue();
            LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | in for amount: " + amount);

            // 不同资源类型的经验转换倍率
            switch (resourceType) {
                case "IRON":
                    // IRON 不用转换
                    totalExp += amount;
                    break;
                case "GOLD":
                    // GOLD 除以3
                    totalExp += amount * 3;
                    break;
                case "DIAMOND":
                    // DIAMOND 除以40
                    totalExp += amount * 40;
                    break;
                case "EMERALD":
                    // EMERALD 除以80
                    totalExp += amount * 80;
                    break;
                default:
                    // 其他资源类型直接加上原值
                    totalExp += amount;
                    break;
            }
        }

        // 加上被击杀者的经验等级
        int playerLevel = gamePlayer.getPlayer().getLevel();
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | playerLevel: " + playerLevel);
        totalExp += playerLevel;
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | totalExp: " + totalExp);

        // 给予击杀者经验
        gameKiller.getPlayer().giveExpLevels(totalExp);
        gameKiller.sendMessage("&a击杀 &e" + gamePlayer.getName() + " &a掠夺了 &e" + totalExp + " &a经验");
    }

    /**
     * 将被击杀者的经验来源转换为物品给予击杀者
     *
     * @param gamePlayer 被击杀的游戏玩家
     * @param gameKiller 击杀者
     * @param event      死亡事件
     */
    private static void convertExperienceSourcesToItems(GamePlayer gamePlayer, GamePlayer gameKiller, PlayerDeathEvent event) {
        Player killer = gameKiller.getPlayer();
        Inventory killerInventory = killer.getInventory();
        Map<String, Integer> expSources = gamePlayer.getExperienceSources();
        List<ItemStack> drops = new ArrayList<>();

        // 从经验源转换为相应的物品数量
        for (Map.Entry<String, Integer> entry : expSources.entrySet()) {
            String resourceType = entry.getKey();
            int expAmount = entry.getValue();
            int itemAmount;
            Material material;

            // 根据资源类型进行转换
            switch (resourceType) {
                case "IRON":
                    // IRON 不用转换
                    itemAmount = expAmount;
                    material = XMaterial.IRON_INGOT.get();
                    break;
                case "GOLD":
                    // GOLD 除以3
                    itemAmount = (int) Math.floor(expAmount / 3.0);
                    material = XMaterial.GOLD_INGOT.get();
                    break;
                case "DIAMOND":
                    // DIAMOND 除以40
                    itemAmount = (int) Math.floor(expAmount / 40.0);
                    material = XMaterial.DIAMOND.get();
                    break;
                case "EMERALD":
                    // EMERALD 除以80
                    itemAmount = (int) Math.floor(expAmount / 80.0);
                    material = XMaterial.EMERALD.get();
                    break;
                default:
                    continue;
            }

            // 如果转换后数量大于0，创建物品堆并添加到掉落列表
            if (itemAmount > 0) {
                // 创建物品 - 每个物品栈最多64个
                while (itemAmount > 0) {
                    int stackSize = Math.min(itemAmount, 64);
                    ItemStack item = null;
                    if (material != null) {
                        item = new ItemStack(material, stackSize);
                    }
                    drops.add(item);
                    itemAmount -= stackSize;
                }
            }
        }

        // 给予击杀者物品或掉落在地上
        for (ItemStack item : drops) {
            // 尝试添加到击杀者背包
            HashMap<Integer, ItemStack> leftover = killerInventory.addItem(item);

            // 如果有剩余，掉落在死亡玩家位置
            if (!leftover.isEmpty()) {
                for (ItemStack leftItem : leftover.values()) {
                    gamePlayer.getPlayer().getWorld().dropItem(gamePlayer.getPlayer().getLocation(), leftItem);
                }
            }
        }

        // 清空死亡玩家的物品栏
        gamePlayer.getPlayer().getInventory().clear();

        // 更新两个玩家的背包显示
        gamePlayer.getPlayer().updateInventory();
        killer.updateInventory();
    }

    /**
     * 将死亡玩家的物品转移给击杀者
     *
     * @param player 死亡玩家
     * @param killer 击杀者
     * @param event  死亡事件
     */
    private static void transferItemsToKiller(Player player, Player killer, PlayerDeathEvent event) {
        Inventory playerInventory = player.getInventory();
        Inventory killerInventory = killer.getInventory();

        // 定义需要转移的资源类型
        Material[] resourceTypes = {
                XMaterial.IRON_INGOT.get(),
                XMaterial.GOLD_INGOT.get(),
                XMaterial.DIAMOND.get(),
                XMaterial.EMERALD.get()
        };

        // 先把所有物品添加到掉落列表中
        List<ItemStack> drops = new ArrayList<>();

        // 收集所有死亡玩家的资源物品
        for (ItemStack item : playerInventory.getContents()) {
            if (item == null) {
                continue;
            }

            Material itemType = item.getType();
            boolean isResource = false;

            // 检查是否为资源物品
            for (Material resourceType : resourceTypes) {
                if (resourceType.equals(itemType)) {
                    isResource = true;
                    break;
                }
            }

            // 如果是资源物品，加入掉落列表
            if (isResource) {
                drops.add(item.clone());
            }
        }

        // 尝试将资源给击杀者
        for (ItemStack item : drops) {
            // 尝试添加到击杀者背包
            HashMap<Integer, ItemStack> leftover = killerInventory.addItem(item);

            // 如果有剩余，掉落在死亡玩家位置
            if (!leftover.isEmpty()) {
                for (ItemStack leftItem : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), leftItem);
                }
            }
        }

        // 清空死亡玩家的物品栏
        playerInventory.clear();

        // 更新两个玩家的背包显示
        player.updateInventory();
        killer.updateInventory();
    }

}
