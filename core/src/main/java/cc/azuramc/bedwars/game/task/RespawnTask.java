package cc.azuramc.bedwars.game.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.config.object.shop.TierChains;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.shop.ShopCatalog;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Holds a player at the respawn point as a spectator, then puts them back at their base
 *
 * @author an5w1r@163.com
 */
public class RespawnTask extends BukkitRunnable {


    private final GameManager gameManager;
    private final GamePlayer gamePlayer;
    private int secondsLeft;

    public RespawnTask(GameManager gameManager, GamePlayer gamePlayer, int seconds) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.secondsLeft = seconds;
    }

    public static int defaultSeconds() {
        return config().getRespawnCountdownSeconds();
    }

    public void start() {
        Player player = gamePlayer.getPlayer();
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0.0F);
        player.teleport(gameManager.getRespawnLocation());
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);
        gamePlayer.setRespawning(true);
        runTaskTimer(AzuraBedWars.getInstance().getPlugin(), 20L, 20L);
    }

    @Override
    public void run() {
        if (!gamePlayer.getPlayer().isOnline() || gameManager.getGameState() != GameState.RUNNING) {
            cancel();
            return;
        }
        if (secondsLeft > 0) {
            gamePlayer.sendTitle(String.format(messages().getRespawnCountdownTitle(), secondsLeft), messages().getRespawnCountdownSubTitle(),
                    1, config().getTitleStay(), 1);
            secondsLeft--;
            return;
        }
        cancel();
        respawn();
    }

    private void respawn() {
        Player player = gamePlayer.getPlayer();
        player.setExp(0f);
        player.setLevel(0);

        gamePlayer.setReconnect(false);
        gamePlayer.setRespawning(false);

        for (Map.Entry<String, TierChains.Chain> entry : ShopCatalog.get().getTiers().entrySet()) {
            TierChains.Chain chain = entry.getValue();
            int level = gamePlayer.getTierLevel(entry.getKey());
            if (chain.getOnDeath() == TierChains.OnDeath.DOWNGRADE && level > 1) {
                gamePlayer.getTierLevels().put(entry.getKey(), level - 1);
            } else if (chain.getOnDeath() == TierChains.OnDeath.RESET) {
                gamePlayer.getTierLevels().remove(entry.getKey());
            }
        }
        gamePlayer.giveInventory();

        PlayerUtil.showPlayer(GamePlayer.getOnlinePlayers(), gamePlayer);
        player.teleport(gamePlayer.getGameTeam().getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        gamePlayer.protectAfterRespawn(config().getRespawnProtectionTicks());

        gamePlayer.sendTitle(messages().getRespawnCompleteTitle(), messages().getRespawnCompleteSubTitle(), 1, config().getTitleStay(), 1);
    }

    private static GameplayConfig.PlayerRespawn config() {
        return AzuraBedWars.getInstance().getGameplayConfig().getPlayerRespawn();
    }

    private static MessageConfig messages() {
        return AzuraBedWars.getInstance().getMessageConfig();
    }
}
