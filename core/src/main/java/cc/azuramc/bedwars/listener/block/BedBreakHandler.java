package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.compat.util.BedUtil;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.VaultUtil;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author an5w1r@163.com
 */
public class BedBreakHandler {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final GameManager GAME_MANAGER = PLUGIN.getGameManager();

    private static final EventConfig.DestroyBedEvent CONFIG = AzuraBedWars.getInstance().getEventConfig().getDestroyBedEvent();

    private static final int BED_SEARCH_RADIUS = CONFIG.getBedSearchRadius();
    private static final int BED_DESTROY_REWARD = CONFIG.getBedDestroyReward();

    /**
     * 处理床方块破坏
     *
     * @param event 方块破坏事件
     * @param block 方块
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     */
    public static void handleBedBreak(BlockBreakEvent event, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        // 不能破坏自己的床
        if (gameTeam.getSpawnLocation().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
            gamePlayer.sendMessage("§c你不能破坏你家的床");
            return;
        }

        // 查找床所属团队
        for (GameTeam targetTeam : GAME_MANAGER.getGameTeams()) {
            if (targetTeam.getSpawnLocation().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
                if (!targetTeam.isDead()) {
                    processBedDestruction(gamePlayer, gameTeam, targetTeam, block);
                    return;
                }
                gamePlayer.sendMessage("§c此床没有队伍");
                return;
            }
        }
    }

    /**
     * 处理床被破坏的逻辑
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     * @param targetTeam 床所属团队
     * @param block 床方块
     */
    private static void processBedDestruction(GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam, Block block) {

        // 触发床被破坏事件
        BedwarsDestroyBedEvent event = new BedwarsDestroyBedEvent(gamePlayer, targetTeam);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        // 掉落床方块物品
        BedUtil.dropTargetBlock(block);

        // 奖励金币
        rewardBedDestruction(gamePlayer);

        // 广播消息
        broadcastBedDestructionMessages(gamePlayer, gameTeam, targetTeam);

        // 更新团队状态
        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setDestroyed(true);

        // 更新玩家统计数据
        gamePlayer.getPlayerData().addDestroyedBeds();
    }

    /**
     * 奖励破坏床的玩家金币
     *
     * @param gamePlayer 游戏玩家
     */
    private static void rewardBedDestruction(GamePlayer gamePlayer) {
        // 动作栏显示奖励
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 5) {
                    cancel();
                    return;
                }
                gamePlayer.sendActionBar("§6+" + BED_DESTROY_REWARD + "个金币");
                i++;
            }
        }.runTaskTimerAsynchronously(PLUGIN, 0, 10);

        if (!VaultUtil.ecoIsNull) {
            gamePlayer.sendMessage("§6+" + BED_DESTROY_REWARD + "个金币 (破坏床)");
            VaultUtil.depositPlayer(gamePlayer, BED_DESTROY_REWARD);
        }
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
        GAME_MANAGER.broadcastSound(XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 10, 10);

        // 发送全局消息
        GAME_MANAGER.broadcastMessage(" ");
        GAME_MANAGER.broadcastMessage("&f&l床被破坏 >>> " + targetTeam.getChatColor() + targetTeam.getName() + " 的床 &7被 " + gameTeam.getChatColor() + gamePlayer.getNickName() + " 破坏");
        GAME_MANAGER.broadcastMessage(" ");

        // 向受影响的团队发送标题提示
        GAME_MANAGER.broadcastTeamTitle(targetTeam, "§c§l床被摧毁", "§c死亡将无法复活", 1, 20, 1);
    }


}
