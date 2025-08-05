package cc.azuramc.bedwars.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.listener.block.BlockBreakListener;
import cc.azuramc.bedwars.listener.block.PlacementListener;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.listener.player.*;
import cc.azuramc.bedwars.listener.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listener.projectile.FireballListener;
import cc.azuramc.bedwars.listener.server.ServerListener;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.listener.special.*;
import cc.azuramc.bedwars.listener.spectator.SpectatorCompassListener;
import cc.azuramc.bedwars.listener.spectator.SpectatorInteractAtEntityListener;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import cc.azuramc.bedwars.listener.world.ExplodeListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * @author an5w1r@163.com
 */
public class ListenerRegistry {
    private final AzuraBedWars plugin;

    public ListenerRegistry(AzuraBedWars plugin) {
        this.plugin = plugin;

        // block package
        register(new BlockBreakListener());
        register(new BlockBreakListener());
        register(new PlacementListener());

        // chat package
        register(new ChatListener());

        // player package
        register(new PlayerAFKListener());
        register(new PlayerChestOpenListener());
        register(new PlayerDamageListener());
        register(new PlayerDeathReasonListener());
        register(new PlayerDragToolListener());
        register(new PlayerDrinkMilkListener(plugin));
        register(new PlayerDropListener());
        register(new PlayerFastPutListener());
        register(new PlayerInteractShopListener());
        register(new PlayerInvisibilityListener(plugin));
        register(new PlayerJoinListener());
        register(new PlayerLobbyInteractListener());
        register(new PlayerMiscListener());
        if (VersionUtil.isLessThan1_13()) {
            register(new PlayerPickUpListenerLowVersion());
        } else {
            register(new PlayerPickUpListenerHighVersion());
        }
        register(new PlayerQuitListener());
        register(new PlayerRespawnListener());
        register(new PlayerRightClickListener());
        register(new PlayerTntDamageListener());
        register(new PlayerUseBucketListener());

        // projectile package
        register(new EggBridgeListener());
        register(new FireballListener());

        // server package
        register(new ServerListener());

        // setup package
        if (plugin.getSettingsConfig().isEditorMode()) {
            new SetupItemListener(plugin);
        }

        // special package
        register(new EntityDeathListener());
        register(new CustomEntityDamageListener());
        register(new IronGolemSpawnListener());
        register(new RescuePlatformListener());
        register(new SilverFishListener());
        register(new WarpPowderListener());

        // spectator package
        register(new SpectatorCompassListener());
        register(new SpectatorInteractAtEntityListener());

        // world
        register(new ChunkListener());
        register(new ExplodeListener());
    }

    private void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
