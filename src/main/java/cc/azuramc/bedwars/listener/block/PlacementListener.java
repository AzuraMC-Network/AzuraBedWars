package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * @author an5w1r@163.com
 */
public class PlacementListener implements Listener {

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

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

        // 游戏未开始时不允许放置方块
        if (GAME_MANAGER.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        // 玩家为观察者时不允许放置方块
        if (gamePlayer != null && gamePlayer.isSpectator()) {
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
        if (block.getType() == XMaterial.TNT.get()) {
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
        return item != null && isWoolMaterialByName(item.getType()) && !item.getEnchantments().isEmpty();
    }

    /**
     * 通过检查 XMaterial 名称是否以 "_WOOL" 结尾来判断是否是羊毛。
     *
     * @param material 要检查的 Material
     * @return 如果物品材质名称以 _WOOL 结尾则返回 true，否则 false
     */
    private boolean isWoolMaterialByName(Material material) {
        Optional<XMaterial> xMaterialOptional = Optional.of(XMaterial.matchXMaterial(material));

        // 检查 XMaterial 的名称是否包含 "WOOL"
        return xMaterialOptional.map(xMat -> xMat.name().contains("WOOL"))
                .orElse(false);
    }

}
