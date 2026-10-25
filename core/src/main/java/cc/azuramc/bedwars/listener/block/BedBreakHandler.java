package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.bed.BedwarsBedDestroyEvent;
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

    public static void handleBedBreak(BlockBreakEvent event, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        if (gameTeam.isBedBlock(block)) {
            gamePlayer.sendMessage("§c你不能破坏你家的床");
            return;
        }

        for (GameTeam targetTeam : GAME_MANAGER.getGameTeams()) {
            if (targetTeam.isBedBlock(block)) {
                if (targetTeam.isDead()) {
                    gamePlayer.sendMessage("§c此床没有队伍");
                } else {
                    processBedDestruction(gamePlayer, gameTeam, targetTeam);
                }
                return;
            }
        }
    }

    private static void processBedDestruction(GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam) {
        BedwarsBedDestroyEvent.Pre preEvent = new BedwarsBedDestroyEvent.Pre(gamePlayer, targetTeam, GAME_MANAGER);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }

        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setDestroyed(true);

        gamePlayer.recordBedBreak();

        String message = "&f&l床被破坏 >>> " + targetTeam.getChatColor() + targetTeam.getName() + " 的床 &7被 " + gameTeam.getChatColor() + gamePlayer.getNickName() + " 破坏";
        String title = "§c§l床被摧毁";
        String subTitle = "§c死亡将无法复活";
        BedwarsBedDestroyEvent.Post postEvent = new BedwarsBedDestroyEvent.Post(gamePlayer, targetTeam, GAME_MANAGER, message, title, subTitle);
        Bukkit.getPluginManager().callEvent(postEvent);

        rewardBedDestruction(gamePlayer);
        broadcastBedDestructionMessages(postEvent);
    }

    private static void rewardBedDestruction(GamePlayer gamePlayer) {
        if (VaultUtil.ecoIsNull) {
            return;
        }
        gamePlayer.sendMessage("§6+" + settingsConfig.getBedDestroyReward() + "个金币 (破坏床)");
        VaultUtil.depositPlayer(gamePlayer, settingsConfig.getBedDestroyReward());
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
        }.runTaskTimer(PLUGIN.getPlugin(), 0, 10);
    }

    private static void broadcastBedDestructionMessages(BedwarsBedDestroyEvent.Post event) {
        GAME_MANAGER.broadcastSound(XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 10, 10);

        if (event.getMessage() != null) {
            GAME_MANAGER.broadcastMessage(" ", event.getMessage(), " ");
        }
        if (event.getTitle() != null || event.getSubTitle() != null) {
            GAME_MANAGER.broadcastTeamTitle(event.getGameTeam(), event.getTitle(), event.getSubTitle(), 1, 20, 1);
        }
    }
}
