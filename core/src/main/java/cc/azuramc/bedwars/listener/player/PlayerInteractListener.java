package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.shop.gui.TeamShopGUI;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author an5w1r@163.com
 */
public class PlayerInteractListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Material interactingMaterial = event.getMaterial();

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }


        if (handleSpectatorCompassNavigation(event, gamePlayer, interactingMaterial)) {
            return;
        }
    }

    /**
     * 处理旁观者使用指南针的导航功能
     * @return 如果事件被处理并应该结束，返回true
     */
    private boolean handleSpectatorCompassNavigation(PlayerInteractEvent event, GamePlayer gamePlayer, Material material) {
        // 检查是否为左键点击动作
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR || 
                              event.getAction() == Action.LEFT_CLICK_BLOCK;
        
        // 检查是否有旁观目标且物品是指南针
        boolean hasSpectatorTargetWithCompass = gamePlayer.getSpectatorTarget() != null && 
                                                material == XMaterial.COMPASS.get();
        
        // 如果同时满足上述条件，执行传送
        if (isLeftClick && hasSpectatorTargetWithCompass) {
            gamePlayer.getSpectatorTarget().tp();
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        if (gamePlayer == null || gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(),
                () -> player.getInventory().removeItem(XMaterial.BUCKET.parseItem()), 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && gameManager.getGameState() == GameState.RUNNING) {
            handleSpectatorEntityInteraction(event, gamePlayer);
        }
    }

    /**
     * 处理旁观者与实体的交互
     */
    private void handleSpectatorEntityInteraction(PlayerInteractAtEntityEvent event, GamePlayer gamePlayer) {
        if (event.getRightClicked() instanceof Player targetPlayer && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRST_PERSON)) {
            event.setCancelled(true);

            if (GamePlayer.get(targetPlayer.getUniqueId()).isSpectator()) {
                return;
            }

            enableFirstPersonSpectating(gamePlayer, event.getPlayer(), targetPlayer);
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * 启用第一人称旁观模式
     */
    private void enableFirstPersonSpectating(GamePlayer gamePlayer, Player spectator, Player target) {
        gamePlayer.sendTitle("§a正在旁观§7" + target.getName(), "§a点击左键打开菜单  §c按Shift键退出", 0, 20, 0);
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setSpectatorTarget(target);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().hasMetadata("Shop")) {
            handleItemShopInteraction(event, gamePlayer);
            return;
        }

        if (event.getRightClicked().hasMetadata("Shop2")) {
            handleTeamShopInteraction(event, gamePlayer);
        }
    }
    
    /**
     * 处理物品商店交互
     */
    private void handleItemShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new ItemShopGUI(gamePlayer, 0, gameManager).open();
    }
    
    /**
     * 处理队伍商店交互
     */
    private void handleTeamShopInteraction(PlayerInteractEntityEvent event, GamePlayer gamePlayer) {
        event.setCancelled(true);
        if (gamePlayer.isSpectator()) {
            return;
        }
        new TeamShopGUI(gamePlayer, gameManager).open();
    }
}