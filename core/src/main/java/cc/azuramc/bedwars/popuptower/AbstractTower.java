package cc.azuramc.bedwars.popuptower;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.TeamColor;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * @author An5w1r@163.com
 */
public abstract class AbstractTower {
    private static final int BLOCKS_PER_TICK = 2;
    private static final long TICK_DELAY = 1L;

    private BukkitTask buildTask;

    public AbstractTower(Location location, Block chest, TeamColor color, Player player) {
        consumeItemFromHand(player);
        List<String> coordinates = getTowerCoordinates();
        startBuilding(coordinates, chest, color, player);
    }

    /**
     * 子类提供具体的坐标数据
     */
    protected abstract List<String> getTowerCoordinates();

    /**
     * 消耗玩家手中的物品
     */
    private void consumeItemFromHand(Player player) {
        ItemStack itemInHand = PlayerUtil.getItemInHand(player);
        if (itemInHand == null) return;

        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            PlayerUtil.setItemInHand(player, null);
        }
    }

    /**
     * 开始建造塔
     */
    private void startBuilding(List<String> coordinates, Block chest, TeamColor color, Player player) {
        final int[] currentIndex = {0};
        final int totalBlocks = coordinates.size();

        this.buildTask = Bukkit.getScheduler().runTaskTimer(
                AzuraBedWars.getInstance(),
                () -> {
                    GamePlayer.get(player).playSound(XSound.ENTITY_CHICKEN_EGG.get(), 1.0F, 0.5F);

                    for (int i = 0; i < BLOCKS_PER_TICK && currentIndex[0] < totalBlocks; i++) {
                        String coordinate = coordinates.get(currentIndex[0]);
                        placeBlock(coordinate, chest, color);
                        currentIndex[0]++;
                    }

                    if (currentIndex[0] >= totalBlocks) {
                        this.buildTask.cancel();
                    }
                },
                0L,
                TICK_DELAY
        );
    }

    /**
     * 放置单个方块
     */
    private void placeBlock(String coordinate, Block chest, TeamColor color) {
        if (coordinate.contains("ladder")) {
            String[] parts = coordinate.split("ladder");
            int ladderData = Integer.parseInt(parts[1]);
            new NewPlaceBlock(chest, coordinate, color, true, ladderData);
        } else {
            new NewPlaceBlock(chest, coordinate, color, false, 0);
        }
    }

    /**
     * 取消建造任务
     */
    public void cancelBuilding() {
        if (buildTask != null && !buildTask.isCancelled()) {
            buildTask.cancel();
        }
    }
}