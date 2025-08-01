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

import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class SpectatorManager {

    private static final List<GamePlayer> spectators = new ArrayList<>();

    public static void add(GamePlayer gamePlayer) {
        gamePlayer.setSpectator(true);
        spectators.add(gamePlayer);
    }

    public static void remove(GamePlayer gamePlayer) {
        if (!spectators.contains(gamePlayer)) {
            return;
        }
        gamePlayer.setSpectator(false);
        spectators.remove(gamePlayer);
    }

    public static void clearSpectators() {
        spectators.clear();
    }

    /**
     * 将玩家转换为观察者
     */
    public static void toSpectator(GamePlayer gamePlayer) {
        SpectatorManager.add(gamePlayer);
        gamePlayer.setSpectatorTarget(new SpectatorTarget(gamePlayer, null));

        Player player = gamePlayer.getPlayer();
        setupSpectatorInventory(player);
        setupSpectatorEffects(player);
        setupSpectatorTarget(gamePlayer);
    }

    /**
     * 将玩家转换为观察者
     */
    public static void toSpectator(GamePlayer gamePlayer, String title, String subTitle) {
        SpectatorManager.add(gamePlayer);
        gamePlayer.setSpectatorTarget(new SpectatorTarget(gamePlayer, null));

        Player player = gamePlayer.getPlayer();
        setupSpectatorPlayer(gamePlayer, title, subTitle);
        setupSpectatorInventory(player);
        setupSpectatorEffects(player);
        setupSpectatorTarget(gamePlayer);
    }

    /**
     * 设置观察者玩家状态
     */
    private static void setupSpectatorPlayer(GamePlayer gamePlayer, String title, String subTitle) {
        gamePlayer.sendTitle(title, subTitle, 10, 20, 10);
        PlayerUtil.hidePlayer(gamePlayer, GamePlayer.getGamePlayers());

        Player player = gamePlayer.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(AzuraBedWars.getInstance().getGameManager().getMapData().getRespawnLocation().toLocation());
    }

    /**
     * 设置观察者物品栏
     */
    private static void setupSpectatorInventory(Player player) {
        player.getInventory().setItem(0, createSpectatorItem(XMaterial.COMPASS.get(), "§a§l传送器 §7(右键打开)"));
        player.getInventory().setItem(4, createSpectatorItem(XMaterial.COMPARATOR.get(), "§c§l旁观者设置 §7(右键打开)"));
        player.getInventory().setItem(7, createSpectatorItem(XMaterial.PAPER.get(), "§b§l快速加入 §7(右键加入)"));
        player.getInventory().setItem(8, createSpectatorItem(XMaterial.SLIME_BALL.get(), "§c§l离开游戏 §7(右键离开)"));
    }

    /**
     * 创建观察者物品
     */
    private static ItemStack createSpectatorItem(Material material, String name) {
        return new ItemBuilder()
                .setType(material)
                .setDisplayName(name)
                .getItem();
    }

    /**
     * 设置观察者效果
     */
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

    /**
     * 设置观察者目标
     */
    private static void setupSpectatorTarget(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam != null && !gameTeam.getAlivePlayers().isEmpty()) {
            gamePlayer.getSpectatorTarget().setTarget(gameTeam.getAlivePlayers().get(0));
        }
    }
}
