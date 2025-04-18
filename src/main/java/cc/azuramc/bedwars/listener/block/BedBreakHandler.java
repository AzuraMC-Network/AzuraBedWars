package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.compat.util.ActionBarUtil;
import cc.azuramc.bedwars.compat.util.BedUtil;
import cc.azuramc.bedwars.api.event.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BedBreakHandler {

    private static final int BED_SEARCH_RADIUS = 18;
    private static final int BED_DESTROY_REWARD = 10;


    private static final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private static final GameManager gameManager = plugin.getGameManager();

    /**
     * 处理床方块破坏
     *
     * @param event 方块破坏事件
     * @param player 玩家
     * @param block 方块
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     */
    public static void handleBedBreak(BlockBreakEvent event, Player player, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        // 不能破坏自己的床
        if (gameTeam.getSpawn().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
            player.sendMessage("§c你不能破坏你家的床");
            return;
        }

        // 查找床所属团队
        for (GameTeam targetTeam : gameManager.getGameTeams()) {
            if (targetTeam.getSpawn().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
                if (!targetTeam.isDead()) {
                    processBedDestruction(player, gamePlayer, gameTeam, targetTeam, block);
                    return;
                }
                player.sendMessage("§c此床没有队伍");
                return;
            }
        }
    }

    /**
     * 处理床被破坏的逻辑
     *
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     * @param targetTeam 床所属团队
     * @param block 床方块
     */
    private static void processBedDestruction(Player player, GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam, Block block) {
        // 掉落床方块物品
        BedUtil.dropTargetBlock(block);

        // 奖励金币
        rewardBedDestruction(player);

        // 广播消息
        broadcastBedDestructionMessages(gamePlayer, gameTeam, targetTeam);

        // 触发床被破坏事件
        Bukkit.getPluginManager().callEvent(new BedwarsDestroyBedEvent(player, targetTeam));

        // 更新团队状态
        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setDestroyed(true);

        // 更新玩家统计数据
        gamePlayer.getPlayerProfile().addDestroyedBeds();
    }

    /**
     * 奖励破坏床的玩家金币
     *
     * @param player 玩家
     */
    private static void rewardBedDestruction(Player player) {
        // 动作栏显示奖励
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 5) {
                    cancel();
                    return;
                }
                ActionBarUtil.sendBar(player, "§6+" + BED_DESTROY_REWARD + "个金币");
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10);

        // 聊天栏显示奖励
        player.sendMessage("§6+" + BED_DESTROY_REWARD + "个金币 (破坏床)");

        // 实际奖励金币
        plugin.getEcon().depositPlayer(player, BED_DESTROY_REWARD);
    }

    /**
     * 广播床被破坏的消息
     *
     * @param gamePlayer 破坏床的玩家
     * @param gameTeam 玩家所在团队
     * @param targetTeam 床所属团队
     */
    private static void broadcastBedDestructionMessages(GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam) {
        // 播放全局音效
        gameManager.broadcastSound(SoundWrapper.ENDERDRAGON_HIT(), 10, 10);

        // 发送全局消息
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage("§c§l" + targetTeam.getName() + " §a的床被 " + gameTeam.getChatColor() + gamePlayer.getNickName() + "§a 挖爆!");
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");

        // 向受影响的团队发送标题提示
        gameManager.broadcastTeamTitle(targetTeam, 1, 20, 1, "§c§l床被摧毁", "§c死亡将无法复活");
    }


}
