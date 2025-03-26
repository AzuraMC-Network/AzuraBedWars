package cc.azuramc.bedwars.game.timer;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CompassRunnable {
    private boolean timer;

    public void start() {
        if (!this.timer) {
            timer = true;

            Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> {
                for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
                    if (gamePlayer.isSpectator()) {
                        SpectatorTarget target = gamePlayer.getSpectatorTarget();
                        target.sendTip();
                        target.autoTp();
                        continue;
                    }
                    Player player = gamePlayer.getPlayer();

                    // 使用兼容方法获取玩家手中的物品
                    ItemStack itemStack = getItemInPlayerHand(player);

                    if (itemStack != null && itemStack.getType() == Material.COMPASS) {
                        gamePlayer.getPlayerCompass().sendClosestPlayer();
                    }
                }
            }, 0L, 1L);
        }
    }

    /**
     * 获取玩家手中的物品，兼容1.8和高版本
     * @param player 玩家
     * @return 玩家手中的物品，如果没有则返回null
     */
    private ItemStack getItemInPlayerHand(Player player) {
        try {
            // 先尝试使用新API (1.9+)
            try {
                return player.getInventory().getItemInMainHand();
            } catch (NoSuchMethodError e) {
                // 如果新API不可用，回退到旧API (1.8)
                return player.getItemInHand();
            }
        } catch (Exception e) {
            // 处理任何可能的异常，确保代码不会崩溃
            System.out.println("获取玩家手中物品时出错: " + e.getMessage());
            return null;
        }
    }
}
