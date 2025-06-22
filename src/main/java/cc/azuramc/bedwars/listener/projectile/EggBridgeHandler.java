package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.team.TeamColor;
import cc.azuramc.bedwars.util.MapUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class EggBridgeHandler implements Runnable {

    @Getter
    private final Egg projectile;
    @Getter
    private final TeamColor teamColor;
    @Getter
    private final Player player;
    private final BukkitTask task;

    private final GameManager gameManager;

    public EggBridgeHandler(AzuraBedWars plugin, Player player, Egg projectile, TeamColor teamColor) {
        this.gameManager = plugin.getGameManager();
        this.projectile = projectile;
        this.teamColor = teamColor;
        this.player = player;
        task = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), this, 0, 1);
    }

    @Override
    public void run() {

        // 扔出搭桥蛋时搭桥蛋的位置
        Location loc = getProjectile().getLocation();

        // 第二个方块（循环的第一个方块）
        Block b2 = loc.clone().subtract(0.0D, 2.0D, 0.0D).getBlock();
        // 第三个方块（循环的第二个方块）
        Block b3 = loc.clone().subtract(1.0D, 2.0D, 0.0D).getBlock();
        // 第四个方块（循环的第三个方块）
        Block b4 = loc.clone().subtract(0.0D, 2.0D, 1.0D).getBlock();
        Block b5 = loc.clone().subtract(-1.0D, 2.0D, 0.0D).getBlock();
        Block b6 = loc.clone().subtract(0.0D, 2.0D, -1.0D).getBlock();
        Block b7 = loc.clone().subtract(1.0D, 2.0D, 1.0D).getBlock();
        Block b8 = loc.clone().subtract(-1.0D, 2.0D, 1.0D).getBlock();
        Block b9 = loc.clone().subtract(1.0D, 2.0D, -1.0D).getBlock();
        Block b10 = loc.clone().subtract(-1.0D, 2.0D, -1.0D).getBlock();

        // 检查是否接触到保护区域
        if (MapUtil.isProtectedArea(b2.getLocation(b2.getLocation())) || MapUtil.isProtectedArea(b3.getLocation()) || MapUtil.isProtectedArea(b4.getLocation())) {
            EggBridgeListener.removeEgg(projectile);
            return;
        }

        // 判断搭桥蛋生效位置条件
        if (getProjectile().isDead()
                || getPlayer().getLocation().distance(getProjectile().getLocation()) > 27
                || getPlayer().getLocation().getY() - getProjectile().getLocation().getY() > 9) {
            EggBridgeListener.removeEgg(projectile);
            return;
        }

        // 从距离玩家 2 格开始搭桥
        if (getPlayer().getLocation().distance(loc) > 3.0D) {
            buildEggBridgeBlock(b2);
            buildEggBridgeBlock(b3);
            buildEggBridgeBlock(b4);
            buildEggBridgeBlock(b5);
            buildEggBridgeBlock(b6);
            buildEggBridgeBlock(b7);
            buildEggBridgeBlock(b8);
            buildEggBridgeBlock(b9);
            buildEggBridgeBlock(b10);
        }
    }

    private void buildEggBridgeBlock(Block block) {
        // 检查为非地图方块
        if (!gameManager.getMapData().hasRegion(block.getLocation())) {
            // 检查是否为空气方块
            if (block.getType() == Material.AIR) {
                // 改变 AIR 为 指定颜色的羊毛
                block.setType(Objects.requireNonNull(XMaterial.matchXMaterial(teamColor.getDyeColor().toString() + "_WOOL").orElse(XMaterial.WHITE_WOOL).get()));
                // 播放超级无敌音效
                getPlayer().playSound(player.getLocation(), XSound.BLOCK_WOOL_STEP.get(), 10F, 1F);
            }
        }
    }

    public void cancel(){
        task.cancel();
    }
}
