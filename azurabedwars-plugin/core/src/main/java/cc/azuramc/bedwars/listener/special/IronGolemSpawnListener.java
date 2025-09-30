package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.CustomEntityManager;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author ant1aura@qq.com
 */
public class IronGolemSpawnListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        ItemStack item = PlayerUtil.getItemInHand(player);

        if (block == null) {
            return;
        }

        if (action != Action.RIGHT_CLICK_BLOCK
                || item == null || item.getType() != XMaterial.WOLF_SPAWN_EGG.get()
                || gameManager.getGameState() != GameState.RUNNING
                || gamePlayer == null) {
            return;
        }

        if (player.getGameMode() == GameMode.SURVIVAL) {
            if (item.getAmount() == 1) {
                PlayerUtil.setItemInHand(player, null);
            } else {
                PlayerUtil.setItemInHand(player, new ItemStack(item.getType(), item.getAmount() - 1));
            }
        }

        int despawn = 120;

        // 获取点击面的位置 这样可以在墙的外侧生成
        BlockFace face = event.getBlockFace();
        Location spawnLocation = block.getRelative(face).getLocation().add(0.5, 0, 0.5);

        if (!isLocationSafe(spawnLocation)) {
            gamePlayer.sendMessage("§c无法在此位置生成铁傀儡，请选择更开阔的区域或确保在地上。");
            event.setCancelled(true);
            return;
        }

        new CustomEntityManager(AzuraBedWars.getInstance().getNmsAccess().spawnIronGolem(spawnLocation, gamePlayer,
                0.25, 100), gamePlayer, despawn);
        event.setCancelled(true);
    }



    /**
     * 检查位置是否安全（适合生成铁傀儡）
     * 检查铁傀儡生成位置的垂直空间（3格高度）是否安全
     *
     * @param location 要检查的位置（铁傀儡的脚部位置）
     * @return 如果位置安全返回true 否则返回false
     */
    private boolean isLocationSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        // 检查脚下是否有可站立的方块（不能是空气）
        Block floorBlock = location.clone().add(0, -1, 0).getBlock();
        if (floorBlock.getType() == Material.AIR || !floorBlock.getType().isSolid()) {
            return false;
        }

        // 检查垂直空间（当前位置及上方2格，共3格高度）
        for (int y = 0; y < 3; y++) {
            Location checkLocation = location.clone().add(0, y, 0);
            Block block = checkLocation.getBlock();

            // 如果不是空气方块或者是流体 则false
            if (block.getType() != Material.AIR && block.getType().isSolid()) {
                return false;
            }
        }

        // 检查2x2的占用空间
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    Location checkLocation = location.clone().add(x - 0.5, y, z - 0.5);
                    Block block = checkLocation.getBlock();

                    if (block.getType() != Material.AIR && block.getType().isSolid()) {
                        return false;
                    }
                }

                // 检查脚下是否有支撑
                Block supportBlock = location.clone().add(x - 0.5, -1, z - 0.5).getBlock();
                if (supportBlock.getType() == Material.AIR || !supportBlock.getType().isSolid()) {
                    return false;
                }
            }
        }

        return true;
    }
}
