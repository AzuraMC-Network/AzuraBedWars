package cc.azuramc.bedwars.example.grapple;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sells as {@code custom: grapple:hook}, the reference is the plugin name in lower case and the id
 *
 * @author an5w1r@163.com
 */
public final class GrapplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        AzuraBedWarsAPI.getRegistry().registerItem(this, "hook", GrappleSettings.class, new GrappleHook());
    }
}
