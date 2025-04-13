package cc.azuramc.bedwars.listener.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.special.RescuePlatform;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 救援平台监听器
 * <p>
 * 监听玩家使用救援平台物品的事件并激活救援平台功能
 * </p>
 */
public class RescuePlatformListener implements Listener {
    
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理玩家交互事件
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
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        // 创建一个临时平台实例以获取物品材质
        RescuePlatform platform = new RescuePlatform();
        if (!event.getMaterial().equals(platform.getItemMaterial())) {
            return;
        }

        // 创建并激活救援平台
        boolean success = platform.create(gamePlayer, gameManager);
        
        // 如果创建成功，播放声音
        if (success) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
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
