package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listeners.block.BlockBreakListener;
import cc.azuramc.bedwars.listeners.chat.ChatListener;
import cc.azuramc.bedwars.listeners.player.PlayerDamageListener;
import cc.azuramc.bedwars.listeners.player.PlayerInventoryListener;
import cc.azuramc.bedwars.listeners.world.ChunkListener;
import cc.azuramc.bedwars.listeners.world.ExplodeListener;
import cc.azuramc.bedwars.listeners.block.PlacementListener;
import cc.azuramc.bedwars.listeners.player.*;
import cc.azuramc.bedwars.listeners.projectile.EggBridgeListener;
import cc.azuramc.bedwars.listeners.server.ServerListener;
import org.bukkit.Bukkit;

public class ListenerHandler {

    public ListenerHandler(AzuraBedWars plugin) {
        // 玩家监听器
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerDamageListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerAFKListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EggBridgeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerInventoryListener(), plugin);

        // 游戏监听器
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ExplodeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlacementListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChunkListener(), plugin);
    }
}
