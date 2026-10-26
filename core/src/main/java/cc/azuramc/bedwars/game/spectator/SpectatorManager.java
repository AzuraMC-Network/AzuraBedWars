package cc.azuramc.bedwars.game.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author an5w1r@163.com
 */
public class SpectatorManager {

    public static void add(GamePlayer gamePlayer) {
        gamePlayer.setSpectator(true);
        PlayerUtil.hidePlayer(GamePlayer.getOnlinePlayers(), gamePlayer);
        for (GamePlayer spectator : GamePlayer.getSpectators()) {
            PlayerUtil.showPlayer(spectator, GamePlayer.getOnlinePlayers());
        }
    }

    public static void toSpectator(GamePlayer gamePlayer) {
        toSpectator(gamePlayer, null, null);
    }

    public static void toSpectator(GamePlayer gamePlayer, String title, String subTitle) {
        SpectatorManager.add(gamePlayer);
        gamePlayer.setSpectatorTarget(new SpectatorTarget(gamePlayer, null));

        Player player = gamePlayer.getPlayer();
        setupSpectatorPlayer(gamePlayer, title, subTitle);
        setupSpectatorInventory(player);
        setupSpectatorEffects(player);
        setupSpectatorTarget(gamePlayer);
    }

    private static void setupSpectatorPlayer(GamePlayer gamePlayer, String title, String subTitle) {
        gamePlayer.sendTitle(title, subTitle, 10, 20, 10);

        Player player = gamePlayer.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(AzuraBedWars.getInstance().getGameManager().getMapData().getRespawnLocation().toLocation());
    }

    private static void setupSpectatorInventory(Player player) {
        player.getInventory().setItem(0, createSpectatorItem(XMaterial.COMPASS.get(), AzuraBedWars.getInstance().getLang().getSpectator().getCompassItem()));
        player.getInventory().setItem(4, createSpectatorItem(XMaterial.COMPARATOR.get(), AzuraBedWars.getInstance().getLang().getSpectator().getSettingsItem()));
        player.getInventory().setItem(7, createSpectatorItem(XMaterial.PAPER.get(), AzuraBedWars.getInstance().getLang().getSpectator().getPlayAgainItem()));
        player.getInventory().setItem(8, createSpectatorItem(XMaterial.SLIME_BALL.get(), AzuraBedWars.getInstance().getLang().getSpectator().getLeaveItem()));
    }

    private static ItemStack createSpectatorItem(Material material, String name) {
        return new ItemBuilder()
                .setType(material)
                .setDisplayName(name)
                .getItem();
    }

    private static void setupSpectatorEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        SpectatorSettings spectatorSettings = SpectatorSettings.get(GamePlayer.get(player));
        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHT_VISION)) {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
        }
    }

    private static void setupSpectatorTarget(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam != null && !gameTeam.getAlivePlayers().isEmpty()) {
            gamePlayer.getSpectatorTarget().setTarget(gameTeam.getAlivePlayers().get(0));
        }
    }
}
