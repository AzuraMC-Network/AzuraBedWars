package cc.azuramc.bedwars.listener.world;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class ExplodeListener implements Listener {

    private static final GameManager GAME_MANAGER = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理实体爆炸事件
     *
     * @param event 实体爆炸事件
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        // 游戏未运行时取消爆炸
        if (GAME_MANAGER.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        // 处理爆炸块列表
        processExplodedBlocks(event);

        // 处理火球爆炸
        if (entity instanceof Fireball) {
            FireballHandler.handleFireballExplosion((Fireball) entity);
        }

        event.setCancelled(true);
    }

    /**
     * 处理爆炸块列表
     *
     * @param event 爆炸事件
     */
    private void processExplodedBlocks(EntityExplodeEvent event) {
        // 创建一个新列表来存储真正需要被爆炸的方块
        List<Block> blocksToExplode = new ArrayList<>();

        for (int i = 0; i < event.blockList().size(); i++) {
            Block block = event.blockList().get(i);

            // 检查受保护区域
            if (GAME_MANAGER.getMapData().hasRegion(block.getLocation())) {
                continue;
            }

            // 只处理玩家放置的方块，保护地图方块
            if (!MapUtil.isProtectedBlockType(block) && !GAME_MANAGER.getBlocksLocation().contains(block.getLocation())) {
                blocksToExplode.add(block);
            }
        }

        // 清空原始列表，避免破坏地图方块
        event.blockList().clear();

        // 处理可爆炸的方块
        for (Block block : blocksToExplode) {
            // 清除方块并显示爆炸效果
            block.setType(Material.AIR);

            // 播放爆炸音效
            try {
                block.getWorld().playSound(block.getLocation(), XSound.ENTITY_GENERIC_EXPLODE.get(), 0.5F, 1.0F);
            } catch (Exception e) {
                // 如果声音效果失败，记录日志但不中断游戏
                LoggerUtil.warn("无法播放爆炸音效: " + e.getMessage());
                e.printStackTrace();
            }

            // 从游戏放置的方块列表中移除
            GAME_MANAGER.getBlocksLocation().remove(block.getLocation());
        }
    }
}
