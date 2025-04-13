package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.MapUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlacementListener implements Listener {

    private static final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理方块放置事件
     *
     * @param event 方块放置事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        Block block = event.getBlock();

        // 游戏未开始或玩家为观察者时不允许放置方块
        if (gamePlayer != null && (gameManager.getGameState() == GameState.WAITING || gamePlayer.isSpectator())) {
            event.setCancelled(true);
            return;
        }

        // 不允许放置床方块
        if (block.getType().toString().startsWith("BED")) {
            event.setCancelled(true);
            return;
        }

        // 检查区域保护
        if (MapUtil.isProtectedArea(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        // 处理TNT放置
        if (block.getType() == MaterialWrapper.TNT()) {
            TNTPlacementHandler.handleTNTPlacement(event, player);
            return;
        }

        // 处理火速羊毛
        ItemStack item = PlayerUtil.getItemInHand(player);
        if (isSpeedWool(item)) {
            handleSpeedWoolPlacement(event, player, item);
        }
    }

    /**
     * 处理火速羊毛放置
     *
     * @param event 方块放置事件
     * @param player 玩家
     * @param item 物品
     */
    private void handleSpeedWoolPlacement(BlockPlaceEvent event, Player player, ItemStack item) {
        // 不对火速羊毛使用全局冷却，允许多个同时进行

        // 防止玩家卡在方块中
        Block block = event.getBlock();
        if (block.getY() != event.getBlockAgainst().getY()) {
            if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)),
                    Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                return;
            }
        }

        // 获取搭桥方向
        BlockFace blockFace = event.getBlockAgainst().getFace(block);

        // 开始火速羊毛搭桥任务
        SpeedWoolHandler.startSpeedWoolTask(block, blockFace, item);
    }

    /**
     * 检查物品是否为火速羊毛
     *
     * @param item 物品
     * @return 如果是火速羊毛返回true，否则返回false
     */
    private boolean isSpeedWool(ItemStack item) {
        return item != null && MaterialWrapper.isWool(item.getType()) && !item.getEnchantments().isEmpty();
    }
}
