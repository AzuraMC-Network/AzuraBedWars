package cc.azuramc.bedwars.listeners.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.game.data.GamePlayer;
import cc.azuramc.bedwars.game.phase.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

/**
 * 物品栏监听器
 * <p>
 * 负责处理玩家物品栏相关的事件，包括：
 * 1. 限制玩家将唯一工具/武器放入箱子
 * 2. 记录玩家打开/关闭物品栏的状态
 * </p>
 */
public class PlayerInventoryListener implements Listener {

    /**
     * 游戏管理器实例
     * 用于获取游戏状态和管理游戏进程
     */
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理物品栏点击事件
     *
     * @param event 物品栏点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 检查游戏是否正在运行
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        // 验证点击者是否为玩家
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // 获取游戏玩家实例
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 获取点击的物品
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        // 检查是否是箱子类型的物品栏
        if (event.getInventory().getType() == InventoryType.CHEST) {
            // 如果是工具或武器，且玩家背包中只有一把，则取消事件
            if (isToolOrSword(clickedItem) && !hasMultipleTools(player, clickedItem.getType())) {
                event.setCancelled(true);
                player.sendMessage("§c你不能将唯一的工具或武器放入箱子中！");
            }
        }
    }

    /**
     * 处理物品栏打开事件
     *
     * @param event 物品栏打开事件
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        // 验证打开者是否为玩家
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // 检查游戏是否正在运行
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        // 获取游戏玩家实例
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 根据物品栏类型更新玩家的物品栏查看状态
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case PLAYER:
                gamePlayer.setViewingInventory(true);
                break;
            case CHEST:
                gamePlayer.setViewingChest(true);
                break;
            case ENDER_CHEST:
                gamePlayer.setViewingEnderChest(true);
                break;
            default:
                break;
        }
    }

    /**
     * 处理物品栏关闭事件
     *
     * @param event 物品栏关闭事件
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // 验证关闭者是否为玩家
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // 检查游戏是否正在运行
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        // 获取游戏玩家实例
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 根据物品栏类型更新玩家的物品栏查看状态
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case PLAYER:
                gamePlayer.setViewingInventory(false);
                break;
            case CHEST:
                gamePlayer.setViewingChest(false);
                break;
            case ENDER_CHEST:
                gamePlayer.setViewingEnderChest(false);
                break;
            default:
                break;
        }
    }

    /**
     * 检查玩家是否拥有多个相同类型的工具或武器
     *
     * @param player 玩家
     * @param material 物品类型
     * @return 如果玩家拥有多个相同类型的工具或武器返回true，否则返回false
     */
    private boolean hasMultipleTools(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查物品是否是工具或武器
     *
     * @param item 物品
     * @return 如果是工具或武器返回true，否则返回false
     */
    private boolean isToolOrSword(ItemStack item) {
        return item != null
                && (item.getType().name().endsWith("_PICKAXE")
                || item.getType().name().endsWith("_AXE")
                || item.getType().name().endsWith("_SWORD")
                || item.getType() == Material.SHEARS);
    }
}
