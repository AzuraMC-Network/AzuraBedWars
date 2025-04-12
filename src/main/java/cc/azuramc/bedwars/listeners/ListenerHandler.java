package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listeners.breaking.BreakListener;
import cc.azuramc.bedwars.listeners.chat.ChatListener;
import cc.azuramc.bedwars.listeners.chunk.ChunkListener;
import cc.azuramc.bedwars.listeners.explosion.ExplodeListener;
import cc.azuramc.bedwars.listeners.placement.PlacementListener;
import cc.azuramc.bedwars.listeners.player.*;
import cc.azuramc.bedwars.listeners.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listeners.server.ServerListener;
import org.bukkit.Bukkit;

public class ListenerHandler {

    public ListenerHandler(AzuraBedWars plugin) {
        // 玩家监听器
        Bukkit.getPluginManager().registerEvents(new JoinListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new QuitListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new AFKListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EggBridgeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), plugin);

        // 游戏监听器
        Bukkit.getPluginManager().registerEvents(new BreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ExplodeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlacementListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), plugin);
    }
}
