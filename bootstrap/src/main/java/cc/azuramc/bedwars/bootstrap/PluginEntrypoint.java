package cc.azuramc.bedwars.bootstrap;

/**
 * Plugin body inside the isolated class loader, implementations need a (JavaPlugin) constructor
 *
 * @author an5w1r@163.com
 */
public interface PluginEntrypoint {

    void onLoad();

    void onEnable();

    void onDisable();
}
