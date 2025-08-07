package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.CustomEntityManager;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        Location loc = null;
        if (block != null) {
            loc = block.getLocation();
        }

        if (loc == null) {
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

        // 寻找安全的生成位置
        Location safeLocation = findSafeSpawnLocation(loc.clone().add(0, 1, 0));
        if (safeLocation == null) {
            // 如果找不到安全位置 给玩家提示
            gamePlayer.sendMessage("§c无法在此位置生成铁傀儡，请选择更开阔的区域！");
            event.setCancelled(true);
            return;
        }

        new CustomEntityManager(AzuraBedWars.getInstance().getNmsAccess().spawnIronGolem(safeLocation, gamePlayer,
                0.25, 100, despawn), gamePlayer, despawn);
        event.setCancelled(true);
    }

    /**
     * 寻找安全的铁傀儡生成位置
     * 铁傀儡需要3格高度的垂直空间
     *
     * @param originalLocation 原始生成位置
     * @return 安全的生成位置，如果找不到则返回null
     */
    private Location findSafeSpawnLocation(Location originalLocation) {
        // 首先检查原始位置是否安全
        if (isLocationSafe(originalLocation)) {
            return originalLocation;
        }

        // 如果原始位置不安全 在附近3x3范围内寻找替代位置
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // 跳过原始位置
                if (x == 0 && z == 0) {
                    continue;
                }

                Location testLocation = originalLocation.clone().add(x, 0, z);
                if (isLocationSafe(testLocation)) {
                    return testLocation;
                }
            }
        }

        // 找不到安全位置
        return null;
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

        // 检查生成位置是否在保护区域内
        if (MapUtil.isProtectedArea(location)) {
            return false;
        }

        // 检查垂直空间（当前位置及上方2格，共3格高度）
        for (int y = 0; y < 3; y++) {
            Location checkLocation = location.clone().add(0, y, 0);
            Block block = checkLocation.getBlock();

            // 如果不是空气方块 则位置不安全
            if (block.getType() != Material.AIR) {
                return false;
            }
        }

        return true;
    }
}
