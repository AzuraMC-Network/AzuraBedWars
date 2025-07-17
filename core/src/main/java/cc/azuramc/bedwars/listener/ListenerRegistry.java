package cc.azuramc.bedwars.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.listener.block.BlockBreakListener;
import cc.azuramc.bedwars.listener.block.PlacementListener;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.listener.player.*;
import cc.azuramc.bedwars.listener.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listener.server.ServerListener;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.listener.special.RescuePlatformListener;
import cc.azuramc.bedwars.listener.special.WarpPowderListener;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import cc.azuramc.bedwars.listener.world.ExplodeListener;
import org.bukkit.Bukkit;

/**
 * @author an5w1r@163.com
 */
public class ListenerRegistry {

    public ListenerRegistry(AzuraBedWars plugin) {
        // block package
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlacementListener(), plugin);

        // chat package
        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);

        // player package
        Bukkit.getPluginManager().registerEvents(new PlayerAFKListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathReasonListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDragToolListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerMiscListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerResourcePutListener(), plugin);
        if (VersionUtil.isLessThan113()) {
            Bukkit.getPluginManager().registerEvents(new PlayerPickUpListenerA(), plugin);
        } else {
            Bukkit.getPluginManager().registerEvents(new PlayerPickUpListenerB(), plugin);
        }
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), plugin);

        // projectile package
        Bukkit.getPluginManager().registerEvents(new EggBridgeListener(), plugin);

        // server package
        Bukkit.getPluginManager().registerEvents(new ServerListener(), plugin);

        // setup package
        if (plugin.getSettingsConfig().isEditorMode()) {
            new SetupItemListener(plugin);
        }

        // special package
        Bukkit.getPluginManager().registerEvents(new RescuePlatformListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new WarpPowderListener(), plugin);

        // world
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ExplodeListener(), plugin);
    }
}
