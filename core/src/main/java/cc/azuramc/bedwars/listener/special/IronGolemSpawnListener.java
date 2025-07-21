package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.CustomEntityRemoverUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
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

        GameTeam gameTeam = gamePlayer.getGameTeam();
        int despawn = 240;

        new CustomEntityRemoverUtil(AzuraBedWars.getInstance().getNmsAccess().spawnIronGolem(loc.add(0, 1, 0), gameTeam,
                0.25, 100, despawn), gameTeam, despawn);
        event.setCancelled(true);
    }
}
