package cc.azuramc.bedwars.listeners.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.utils.world.MapUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SpeedWoolHandler {

    private static final int MAX_SPEED_WOOL_LENGTH = 6;
    private static final String SPEED_WOOL_METADATA = "SPEED_WOOL";

    /**
     * 开始火速羊毛搭桥任务
     * 火速羊毛的搭桥速度比大桥蛋更快
     *
     * @param block 起始方块
     * @param blockFace 方向
     * @param item 使用的物品
     */
    public static void startSpeedWoolTask(Block block, BlockFace blockFace, ItemStack item) {
        new BukkitRunnable() {
            int i = 1;
            final UUID taskId = UUID.randomUUID(); // 为每个火速羊毛任务生成一个唯一ID

            @Override
            public void run() {
                if (i > MAX_SPEED_WOOL_LENGTH) {
                    cancel();
                    return;
                }

                Block relativeBlock = block.getRelative(blockFace, i);

                // 检查是否可以在此位置放置方块
                if (MapUtil.isProtectedRelativeLocation(relativeBlock)) {
                    cancel();
                    return;
                }

                // 放置方块
                if (relativeBlock.getType() == MaterialWrapper.AIR()) {
                    relativeBlock.setType(item.getType());
                    if (!VersionUtil.isLessThan113() && item.getData() != null) {
                        MapUtil.setBlockData(relativeBlock, item.getData().getData());
                    }

                    // 为火速羊毛放置的方块添加元数据标记
                    relativeBlock.setMetadata(SPEED_WOOL_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), taskId.toString()));

                    // 播放羊毛放置声音
                    block.getWorld().playSound(relativeBlock.getLocation(), SoundWrapper.STEP_WOOL(), 0.3f, 1.5f);
                }

                i++;
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0, 2L); // 每2刻生成一格，比大桥蛋更快
    }

}
