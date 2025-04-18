package cc.azuramc.bedwars.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.listener.block.BlockBreakListener;
import cc.azuramc.bedwars.listener.block.PlacementListener;
import cc.azuramc.bedwars.listener.chat.ChatListener;
import cc.azuramc.bedwars.listener.player.*;
import cc.azuramc.bedwars.listener.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listener.server.ServerListener;
import cc.azuramc.bedwars.listener.world.ChunkListener;
import cc.azuramc.bedwars.listener.world.ExplodeListener;
import org.bukkit.Bukkit;

public class ListenerRegistry {

    public ListenerRegistry(AzuraBedWars plugin) {
        // 玩家监听器
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerMiscListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerAFKListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EggBridgeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDragToolListener(), plugin);

        if (VersionUtil.isLessThan113()) {
            Bukkit.getPluginManager().registerEvents(new PlayerPickUpListenerA(), plugin);
        } else {
            Bukkit.getPluginManager().registerEvents(new PlayerPickUpListenerB(), plugin);
        }

        // 游戏监听器
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ExplodeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlacementListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), plugin);
    }
}
