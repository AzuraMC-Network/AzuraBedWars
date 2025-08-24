package cc.azuramc.bedwars.loader.config;

import cc.azuramc.bedwars.loader.util.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author An5w1r@163.com
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    public boolean loadConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            if (!configFile.exists()) {
                try (InputStream inputStream = plugin.getResource("config.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, configFile.toPath());
                        LoggerUtil.info("已创建默认配置文件: " + configFile.getPath());
                    } else {
                        LoggerUtil.error("无法找到默认配置文件资源!");
                        return false;
                    }
                }
            }

            config = YamlConfiguration.loadConfiguration(configFile);
            LoggerUtil.info("配置文件加载成功");
            return true;

        } catch (IOException e) {
            LoggerUtil.error("加载配置文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public String getGitHubOwner() {
        return config.getString("github.owner", "AzuraMC-Network");
    }

    public String getGitHubRepo() {
        return config.getString("github.repo", "AzuraBedWars");
    }

    public String getGitHubApiUrl() {
        return config.getString("github.api-url", "https://api.github.com");
    }

    public boolean isIncludePrerelease() {
        return config.getBoolean("download.include-prerelease", false);
    }

    public int getConnectTimeout() {
        return config.getInt("download.connect-timeout", 60);
    }

    public int getReadTimeout() {
        return config.getInt("download.read-timeout", 600);
    }

    public int getRetryCount() {
        return config.getInt("download.retry-count", 5);
    }

    public int getRetryDelay() {
        return config.getInt("download.retry-delay", 5);
    }

    public int getBufferSize() {
        return config.getInt("download.buffer-size", 8192);
    }

    public boolean isNetworkOptimizationEnabled() {
        return config.getBoolean("download.enable-network-optimization", true);
    }

    public int getConnectionTestTimeout() {
        return config.getInt("download.connection-test-timeout", 10);
    }

    public boolean isMultithreadedDownloadEnabled() {
        return config.getBoolean("download.enable-multithreaded-download", true);
    }

    public int getThreadCount() {
        return config.getInt("download.thread-count", 8);
    }

    public String getPluginPrefix() {
        return config.getString("files.plugin-prefix", "AzuraBedWars");
    }

    public String getTempSuffix() {
        return config.getString("files.temp-suffix", ".tmp");
    }

    public String getVersionFile() {
        return config.getString("files.version-file", "version.txt");
    }

    public boolean isVerbose() {
        return config.getBoolean("logging.verbose", true);
    }

    public boolean isShowProgress() {
        return config.getBoolean("logging.show-progress", true);
    }

    public boolean isShutdownOnFailure() {
        return config.getBoolean("error-handling.shutdown-on-failure", true);
    }

    public int getShutdownDelay() {
        return config.getInt("error-handling.shutdown-delay", 5);
    }

    public String getReleasesApiUrl() {
        return String.format("%s/repos/%s/%s/releases",
                getGitHubApiUrl(), getGitHubOwner(), getGitHubRepo());
    }

    public String getLatestReleaseApiUrl() {
        return getReleasesApiUrl() + "/latest";
    }

    public boolean shouldShutdownOnFailure() {
        return config.getBoolean("error_handling.shutdown_on_failure", true);
    }

    public String getVersionFileName() {
        return config.getString("file_management.version_file", "version.txt");
    }
}
