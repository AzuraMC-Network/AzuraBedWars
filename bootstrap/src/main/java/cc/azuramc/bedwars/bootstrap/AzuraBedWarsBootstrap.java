package cc.azuramc.bedwars.bootstrap;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

/**
 * Bukkit entry that loads the plugin body and its libraries in an isolated class loader
 * <p>
 * API classes stay in this jar so other plugins share one copy with the body
 *
 * @author an5w1r@163.com
 */
public final class AzuraBedWarsBootstrap extends JavaPlugin {

    /** Constructed by reflection through its (JavaPlugin) constructor */
    private static final String ENTRYPOINT = "cc.azuramc.bedwars.AzuraBedWars";

    /** Must match options.release in build-logic */
    private static final int REQUIRED_JAVA = 21;

    @Nullable
    private IsolatedClassLoader classLoader;
    @Nullable
    private PluginEntrypoint entrypoint;

    @Override
    public void onLoad() {
        int java = javaVersion();
        if (java < REQUIRED_JAVA) {
            getLogger().severe("AzuraBedWars requires Java " + REQUIRED_JAVA + " or newer, this server runs Java " + java);
            return;
        }
        saveDefaultConfig();
        LibraryResolver resolver = new LibraryResolver(
                getDataFolder().toPath().resolve("libraries"),
                getConfig().getStringList("libraries.repositories"),
                getLogger()
        );
        URL[] classpath;
        try {
            List<Library> libraries = LibraryResolver.readManifest();
            if (getConfig().getBoolean("updater.enabled")) {
                Path target = getServer().getUpdateFolderFile().toPath().resolve(getFile().getName());
                Thread updater = new Thread(new Updater(getLogger(), getDescription().getVersion(),
                        LibraryResolver.bundlesAll(libraries), getConfig().getBoolean("updater.include-prereleases"), target),
                        "AzuraBedWars Updater");
                updater.setDaemon(true);
                updater.start();
            }
            classpath = resolver.resolveAll(libraries);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to prepare runtime libraries, point libraries.repositories in config.yml"
                    + " at reachable repositories or use the offline jar", e);
            return;
        }
        classLoader = new IsolatedClassLoader(classpath, getClassLoader());
        try {
            entrypoint = (PluginEntrypoint) classLoader.loadClass(ENTRYPOINT)
                    .getConstructor(JavaPlugin.class)
                    .newInstance(this);
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, "Failed to construct " + ENTRYPOINT, e);
            closeClassLoader();
            return;
        }
        entrypoint.onLoad();
    }

    @Override
    public void onEnable() {
        if (entrypoint == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        entrypoint.onEnable();
    }

    @Override
    public void onDisable() {
        if (entrypoint != null) {
            entrypoint.onDisable();
            entrypoint = null;
        }
        closeClassLoader();
    }

    private void closeClassLoader() {
        // keeps library files locked on Windows otherwise
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to close the class loader", e);
            }
            classLoader = null;
        }
    }

    private static int javaVersion() {
        String version = System.getProperty("java.specification.version");
        return Integer.parseInt(version.startsWith("1.") ? version.substring(2) : version);
    }
}
