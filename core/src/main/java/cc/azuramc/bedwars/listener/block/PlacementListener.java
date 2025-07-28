package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.popuptower.impl.TowerEast;
import cc.azuramc.bedwars.popuptower.impl.TowerNorth;
import cc.azuramc.bedwars.popuptower.impl.TowerSouth;
import cc.azuramc.bedwars.popuptower.impl.TowerWest;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
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

        // 游戏未开始时不允许放置方块
        if (gameManager.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        // 玩家为观察者时不允许放置方块
        if (gamePlayer != null && gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        // 不允许放置床方块
        if (block.getType().toString().contains("BED")) {
            event.setCancelled(true);
            return;
        }

        // 检查区域保护
        if (MapUtil.isProtectedArea(block.getLocation())) {
            if (gamePlayer != null) {
                gamePlayer.sendMessage("&c你不能在此处放置方块！");
            }
            event.setCancelled(true);
            return;
        }

        // 建筑限高
        if (block.getLocation().getY() > gameManager.getBuildLimitHeight()) {
            if (gamePlayer != null) {
                gamePlayer.sendMessage("&c你不能在此处放置方块！");
            }
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

        // Pop-Up Tower
        if (event.getBlock().getType() == XMaterial.CHEST.get()) {
            handlePopUpTowerPlacement(event, player);
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
     * 处理PopUpTower放置
     * @param event BlockPlaceEvent
     * @param player 玩家
     */
    private void handlePopUpTowerPlacement(BlockPlaceEvent event, Player player) {
        event.setCancelled(true);
        Location loc = event.getBlock().getLocation();
        TeamColor color = GamePlayer.get(player).getGameTeam().getTeamColor();
        double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if (45.0D <= rotation && rotation < 135.0D) {
            new TowerSouth(loc, event.getBlockPlaced(), color, player);
        } else if (225.0D <= rotation && rotation < 315.0D) {
            new TowerNorth(loc, event.getBlockPlaced(), color, player);
        } else if (135.0D <= rotation && rotation < 225.0D) {
            new TowerWest(loc, event.getBlockPlaced(), color, player);
        } else if (0.0D <= rotation && rotation < 45.0D) {
            new TowerEast(loc, event.getBlockPlaced(), color, player);
        } else if (315.0D <= rotation && rotation < 360.0D) {
            new TowerEast(loc, event.getBlockPlaced(), color, player);
        }
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
