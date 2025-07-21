package cc.azuramc.bedwars.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.listener.block.BlockBreakListener;
import cc.azuramc.bedwars.listener.block.PlacementListener;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.listener.player.*;
import cc.azuramc.bedwars.listener.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listener.projectile.FireballListener;
import cc.azuramc.bedwars.listener.special.SilverFishListener;
import cc.azuramc.bedwars.listener.server.ServerListener;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.listener.special.IronGolemSpawnListener;
import cc.azuramc.bedwars.listener.special.RescuePlatformListener;
import cc.azuramc.bedwars.listener.special.WarpPowderListener;
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
        register(new PlayerDropListener());
        register(new PlayerInteractListener());
        register(new PlayerInvisibilityListener(plugin));
        register(new PlayerJoinListener());
        register(new PlayerMiscListener());
        register(new PlayerResourcePutListener());
        if (VersionUtil.isLessThan113()) {
            register(new PlayerPickUpListenerA());
        } else {
            register(new PlayerPickUpListenerB());
        }
        register(new PlayerQuitListener());
        register(new PlayerRespawnListener());
        register(new PlayerTntDamageListener());

        // projectile package
        register(new EggBridgeListener());
        register(new FireballListener());
        register(new SilverFishListener());

        // server package
        register(new ServerListener());

        // setup package
        if (plugin.getSettingsConfig().isEditorMode()) {
            new SetupItemListener(plugin);
        }

        // special package
        register(new RescuePlatformListener());
        register(new WarpPowderListener());
        register(new IronGolemSpawnListener());

        // world
        register(new ChunkListener());
        register(new ExplodeListener());
    }

    private void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
