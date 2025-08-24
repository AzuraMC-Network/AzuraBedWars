package cc.azuramc.bedwars.loader.util;

import cc.azuramc.bedwars.loader.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author An5w1r@163.com
 */
public class PluginLoaderUtil {
    private final ConfigManager configManager;
    private final Logger logger;
    private final PluginManager pluginManager;
    private Plugin loadedPlugin;

    public PluginLoaderUtil(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
        this.pluginManager = Bukkit.getPluginManager();
    }

    /**
     * 加载插件
     *
     * @param pluginFile 插件文件
     * @return 加载成功返回true
     */
    public boolean loadPlugin(File pluginFile) {
        try {
            LoggerUtil.verbose("PluginLoader.loadPlugin 开始执行");
            LoggerUtil.info("正在加载插件: " + pluginFile.getName());

            // 首先卸载已存在的插件
            LoggerUtil.verbose("卸载已存在的插件");
            unloadExistingPlugin();

            // 加载新插件
            LoggerUtil.verbose("调用 pluginManager.loadPlugin");
            Plugin plugin = pluginManager.loadPlugin(pluginFile);
            if (plugin == null) {
                LoggerUtil.error("插件加载失败: 无法从文件加载插件");
                return false;
            }

            LoggerUtil.verbose("插件加载成功，准备启用");
            LoggerUtil.verbose("插件描述: " + plugin.getDescription().getFullName());
            LoggerUtil.verbose("插件主类: " + plugin.getDescription().getMain());
            LoggerUtil.verbose("插件版本: " + plugin.getDescription().getVersion());

            // 启用插件
            LoggerUtil.verbose("调用 pluginManager.enablePlugin");
            pluginManager.enablePlugin(plugin);
            LoggerUtil.verbose("enablePlugin 调用完成");

            if (!plugin.isEnabled()) {
                LoggerUtil.error("插件启用失败");
                LoggerUtil.verbose("plugin.isEnabled() 返回 false");
                return false;
            }

            this.loadedPlugin = plugin;
            LoggerUtil.info("插件加载成功: " + plugin.getDescription().getFullName());
            LoggerUtil.verbose("插件状态: enabled=" + plugin.isEnabled());
            return true;

        } catch (Exception e) {
            LoggerUtil.error("加载插件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 卸载已存在的插件
     */
    private void unloadExistingPlugin() {
        // 查找已加载的AzuraBedWars插件
        Plugin existingPlugin = findExistingPlugin();
        if (existingPlugin != null) {
            LoggerUtil.info("发现已加载的插件，正在卸载: " + existingPlugin.getName());
            unloadPlugin(existingPlugin);
        }
    }

    /**
     * 查找已存在的插件
     */
    private Plugin findExistingPlugin() {
        for (Plugin plugin : pluginManager.getPlugins()) {
            String pluginName = plugin.getName();
            // 检查插件名称是否包含配置的前缀
            if (pluginName.toLowerCase().contains(configManager.getPluginPrefix().toLowerCase())) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * 卸载插件
     */
    private void unloadPlugin(Plugin plugin) {
        try {
            String pluginName = plugin.getName();
            LoggerUtil.verbose("正在卸载插件: " + pluginName);

            // 禁用插件
            pluginManager.disablePlugin(plugin);

            LoggerUtil.verbose("插件卸载完成: " + pluginName);

        } catch (Exception e) {
            logger.warning("卸载插件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public Plugin getLoadedPlugin() {
        return loadedPlugin;
    }

    public boolean isPluginLoaded() {
        return loadedPlugin != null && loadedPlugin.isEnabled();
    }

    public String getPluginInfo() {
        if (loadedPlugin == null) {
            return "没有加载的插件";
        }

        PluginDescriptionFile desc = loadedPlugin.getDescription();
        return String.format("插件: %s v%s (主类: %s)",
                desc.getName(), desc.getVersion(), desc.getMain());
    }
}
