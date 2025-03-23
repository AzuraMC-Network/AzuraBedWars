package cc.azuramc.bedwars;

import cc.azuramc.bedwars.database.mysql.ConnectionPoolHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class AzuraBedWars extends JavaPlugin {

    @Getter
    private static AzuraBedWars instance;
    @Getter
    private ConnectionPoolHandler connectionPoolHandler;


    @Override
    public void onEnable() {
        instance = this;
        connectionPoolHandler = new ConnectionPoolHandler();
    }

    @Override
    public void onDisable() {
    }
}
