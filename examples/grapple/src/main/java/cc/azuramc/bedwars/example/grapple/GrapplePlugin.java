package cc.azuramc.bedwars.example.grapple;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import cc.azuramc.bedwars.api.extension.ExtensionRegistry;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sells as {@code custom: grapple:hook}, the reference is the plugin name in lower case and the id
 *
 * @author an5w1r@163.com
 */
public final class GrapplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ExtensionRegistry registry = AzuraBedWarsAPI.getRegistry();
        GrappleHook hook = new GrappleHook();
        registry.registerItem(this, "hook", GrappleSettings.class, hook);
        JsonObject noArguments = new JsonObject();
        noArguments.addProperty("type", "object");
        registry.registerDevTool(this, "pulls", "Seconds since each online player last pulled with the grappling hook",
                noArguments, true, arguments -> hook.lastPulls());
    }
}
