package cc.azuramc.bedwars.listener.block;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.bed.BedwarsBedDestroyEvent;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.MessageUtil;
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

    public static void handleBedBreak(BlockBreakEvent event, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        if (gameTeam.isBedBlock(block)) {
            gamePlayer.sendMessage(PLUGIN.getLang().getGame().getOwnBed());
            return;
        }

        for (GameTeam targetTeam : GAME_MANAGER.getGameTeams()) {
            if (targetTeam.isBedBlock(block)) {
                if (targetTeam.isDead() && !GAME_MANAGER.isWaitingForReconnect(targetTeam)) {
                    gamePlayer.sendMessage(PLUGIN.getLang().getGame().getBedWithoutTeam());
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

        boolean teamLeft = targetTeam.isDead();
        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setDestroyed(true);

        gamePlayer.recordBedBreak();

        LangConfig.Game lang = PLUGIN.getLang().getGame();
        String message = MessageUtil.fill(lang.getBedBroken(), "team", targetTeam.getChatColor() + targetTeam.getName(),
                "breaker", gameTeam.getChatColor() + gamePlayer.getNickName());
        String title = lang.getBedBrokenTitle();
        String subTitle = lang.getBedBrokenSubtitle();
        BedwarsBedDestroyEvent.Post postEvent = new BedwarsBedDestroyEvent.Post(gamePlayer, targetTeam, GAME_MANAGER, message, title, subTitle);
        Bukkit.getPluginManager().callEvent(postEvent);

        rewardBedDestruction(gamePlayer);
        broadcastBedDestructionMessages(postEvent);
        if (teamLeft) {
            GAME_MANAGER.eliminateOfflineTeam(targetTeam, gamePlayer);
        }
    }

    private static void rewardBedDestruction(GamePlayer gamePlayer) {
        if (VaultUtil.ecoIsNull) {
            return;
        }
        gamePlayer.sendMessage(MessageUtil.fill(PLUGIN.getLang().getGame().getBedReward(), "coins", PLUGIN.getGameplayConfig().getBedDestroyReward()));
        VaultUtil.depositPlayer(gamePlayer, PLUGIN.getGameplayConfig().getBedDestroyReward());
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 5) {
                    cancel();
                    return;
                }
                gamePlayer.sendActionBar(MessageUtil.fill(PLUGIN.getLang().getGame().getBedRewardActionBar(), "coins", PLUGIN.getGameplayConfig().getBedDestroyReward()));
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
