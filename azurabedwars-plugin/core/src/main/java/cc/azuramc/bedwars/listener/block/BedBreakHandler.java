package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.bed.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.hook.VaultUtil;
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
    private static final SettingsConfig settingsConfig = PLUGIN.getSettingsConfig();

    /**
     * 处理床方块破坏
     *
     * @param event      方块破坏事件
     * @param block      方块
     * @param gamePlayer 游戏玩家
     * @param gameTeam   玩家所在团队
     */
    public static void handleBedBreak(BlockBreakEvent event, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        // shouldn't use "==" to compare Block objects (I tested :<)
        if (block.getLocation().equals(gameTeam.getBedFeet().getLocation())
                || block.getLocation().equals(gameTeam.getBedHead().getLocation())) {
            gamePlayer.sendMessage("§c你不能破坏你家的床");
            return;
        }

        // 查找床所属团队
        for (GameTeam targetTeam : GAME_MANAGER.getGameTeams()) {
            if (targetTeam.getSpawnLocation().distance(block.getLocation()) <= settingsConfig.getBedSearchRadius()) {
                if (!targetTeam.isDead()) {
                    processBedDestruction(gamePlayer, gameTeam, targetTeam);
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
     * @param gameTeam   玩家所在团队
     * @param targetTeam 床所属团队
     */
    private static void processBedDestruction(GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam) {

        // 更新团队状态
        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setDestroyed(true);

        // 更新玩家统计数据
        gamePlayer.getPlayerData().addDestroyedBeds();

        // 触发床被破坏事件
        String message = "&f&l床被破坏 >>> " + targetTeam.getChatColor() + targetTeam.getName() + " 的床 &7被 " + gameTeam.getChatColor() + gamePlayer.getNickName() + " 破坏";
        String title = "§c§l床被摧毁";
        String subTitle = "§c死亡将无法复活";
        BedwarsDestroyBedEvent apiEvent = new BedwarsDestroyBedEvent(gamePlayer, targetTeam, GAME_MANAGER, message, title, subTitle);
        Bukkit.getPluginManager().callEvent(apiEvent);

        // 奖励金币
        rewardBedDestruction(gamePlayer);

        // 广播消息
        broadcastBedDestructionMessages(apiEvent);
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
                gamePlayer.sendActionBar("§6+" + settingsConfig.getBedDestroyReward() + "个金币");
                i++;
            }
        }.runTaskTimerAsynchronously(PLUGIN, 0, 10);

        if (!VaultUtil.ecoIsNull) {
            gamePlayer.sendMessage("§6+" + settingsConfig.getBedDestroyReward() + "个金币 (破坏床)");
            VaultUtil.depositPlayer(gamePlayer, settingsConfig.getBedDestroyReward());
        }
    }

    /**
     * 广播床被破坏的消息
     *
     * @param event 床被破坏事件
     */
    private static void broadcastBedDestructionMessages(BedwarsDestroyBedEvent event) {
        // 播放全局音效
        GAME_MANAGER.broadcastSound(XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 10, 10);

        // 发送全局消息
        GAME_MANAGER.broadcastMessage(" ");
        GAME_MANAGER.broadcastMessage(event.getMessage());
        GAME_MANAGER.broadcastMessage(" ");

        // 向受影响的团队发送标题提示
        GAME_MANAGER.broadcastTeamTitle(event.getGameTeam(), event.getTitle(), event.getSubTitle(), 1, 20, 1);
    }


}
