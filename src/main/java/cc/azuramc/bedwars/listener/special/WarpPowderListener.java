package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.item.special.impl.WarpPowder;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * 传送粉末监听器
 * 监听玩家使用传送粉末的事件并处理相关交互
 *
 * @author an5w1r@163.com
 */
public class WarpPowderListener implements Listener {

    private static final ItemConfig.WarpPowder CONFIG = AzuraBedWars.getInstance().getItemConfig().getWarpPowder();
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    
    private static final String CANCEL_ITEM_NAME = CONFIG.getCancelItemName();

    /**
     * 获取玩家当前使用的传送粉末
     * 
     * @param gameManager 游戏实例
     * @param gamePlayer 游戏玩家
     * @return 传送粉末实例或null
     */
    private WarpPowder getActiveWarpPowder(GameManager gameManager, GamePlayer gamePlayer) {
        if (gameManager == null || gamePlayer == null) {
            return null;
        }
        
        for (AbstractSpecialItem item : gameManager.getAbstractSpecialItems()) {
            if (item instanceof WarpPowder powder) {
                if (powder.getPlayer() != null && powder.getPlayer().equals(gamePlayer)) {
                    return powder;
                }
            }
        }

        return null;
    }

    /**
     * 处理玩家受伤事件，取消传送过程
     * 
     * @param event 实体受伤事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        // 检查是否为玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        
        // 检查游戏状态和玩家状态
        if (gameManager.getGameState() != GameState.RUNNING || gamePlayer == null || gamePlayer.isSpectator()) {
            return;
        }

        // 寻找正在使用的传送粉末并取消传送
        WarpPowder powder = getActiveWarpPowder(gameManager, gamePlayer);
        if (powder != null && powder.isTeleporting()) {
            powder.cancelTeleport(true, true);
        }
    }

    /**
     * 处理玩家丢弃物品事件，阻止丢弃取消传送物品
     * 
     * @param event 玩家丢弃物品事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }
        
        ItemStack item = event.getItemDrop().getItemStack();
        ItemMeta meta = item.getItemMeta();
        
        // 检查是否为取消传送物品
        if (meta != null && CANCEL_ITEM_NAME.equals(meta.getDisplayName())) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理玩家交互事件，处理传送粉末使用和取消
     * 
     * @param event 玩家交互事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        // 只处理右键点击
        if (isLeftClick(event.getAction())) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        
        // 检查游戏状态
        if (gameManager.getGameState() != GameState.RUNNING || gamePlayer == null) {
            return;
        }

        // 创建临时实例获取材质
        WarpPowder warpPowder = new WarpPowder();
        ItemStack item = event.getItem();
        
        // 检查物品是否存在
        if (item == null) {
            return;
        }
        
        // 检查物品类型
        Material material = item.getType();
        if (!material.equals(warpPowder.getItemMaterial()) && !material.equals(warpPowder.getActivatedMaterial())) {
            return;
        }

        // 获取玩家已激活的传送粉末
        WarpPowder activePowder = getActiveWarpPowder(gameManager, gamePlayer);

        // 处理取消传送物品
        if (material.equals(warpPowder.getActivatedMaterial())) {
            handleCancelItem(event, item, gamePlayer, activePowder);
            return;
        }

        // 处理传送粉末使用
        handleWarpPowderUse(event, gamePlayer, activePowder, warpPowder);
    }
    
    /**
     * 处理取消传送物品的使用
     * 
     * @param event 交互事件
     * @param item 物品
     * @param gamePlayer 游戏玩家
     * @param activePowder 已激活的传送粉末
     */
    private void handleCancelItem(PlayerInteractEvent event, ItemStack item, GamePlayer gamePlayer, WarpPowder activePowder) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !CANCEL_ITEM_NAME.equals(meta.getDisplayName())) {
            return;
        }

        if (activePowder != null) {
            activePowder.setStackAmount(activePowder.getStack().getAmount() + 1);
            gamePlayer.getPlayer().updateInventory();
            activePowder.cancelTeleport(true, true);
            event.setCancelled(true);
        }
    }
    
    /**
     * 处理传送粉末的使用
     * 
     * @param event 交互事件
     * @param gamePlayer 游戏玩家
     * @param activePowder 已激活的传送粉末
     * @param warpPowder 新的传送粉末
     */
    private void handleWarpPowderUse(PlayerInteractEvent event, GamePlayer gamePlayer, WarpPowder activePowder, WarpPowder warpPowder) {
        // 检查是否已有激活的传送粉末
        if (activePowder != null) {
            gamePlayer.sendMessage("§c你已经开始了一个传送!");
            return;
        }

        // 检查玩家是否在空中
        if (gamePlayer.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            gamePlayer.sendMessage("§c你不能在空中使用传送粉末!");
            return;
        }

        // 激活传送粉末
        warpPowder.setPlayer(gamePlayer);
        boolean success = warpPowder.runTask();
        
        if (success) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理玩家移动事件，取消传送过程
     * 
     * @param event 玩家移动事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMove(PlayerMoveEvent event) {
        // 检查是否只是头部转动
        if (event.getFrom().getBlock().equals(Objects.requireNonNull(event.getTo()).getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        
        // 检查游戏状态
        if (gameManager.getGameState() != GameState.RUNNING || gamePlayer == null) {
            return;
        }

        // 寻找激活的传送粉末并取消传送
        WarpPowder powder = getActiveWarpPowder(gameManager, gamePlayer);
        if (powder != null && powder.isTeleporting()) {
            powder.setStackAmount(powder.getStack().getAmount() + 1);
            player.updateInventory();
            powder.cancelTeleport(true, true);
            player.sendMessage("§c你移动了，传送被取消!");
        }
    }
    
    /**
     * 检查是否为左键点击
     * 
     * @param action 交互动作
     * @return 是否为左键点击
     */
    private boolean isLeftClick(Action action) {
        return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
    }
}
