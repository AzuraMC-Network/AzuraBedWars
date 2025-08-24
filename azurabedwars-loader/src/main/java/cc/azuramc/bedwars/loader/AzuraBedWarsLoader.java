package cc.azuramc.bedwars.loader;

import cc.azuramc.bedwars.loader.config.ConfigManager;
import cc.azuramc.bedwars.loader.download.FileDownloader;
import cc.azuramc.bedwars.loader.download.GitHubReleaseChecker;
import cc.azuramc.bedwars.loader.util.LoggerUtil;
import cc.azuramc.bedwars.loader.util.PluginLoaderUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * @author An5w1r@163.com
 */
public final class AzuraBedWarsLoader extends JavaPlugin {

    private static AzuraBedWarsLoader instance;

    private ConfigManager configManager;
    private GitHubReleaseChecker releaseChecker;
    private FileDownloader fileDownloader;
    private PluginLoaderUtil pluginLoaderUtil;

    public static AzuraBedWarsLoader getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("AzuraBedWars Loader 正在启动...");

        try {
            // 初始化配置管理器
            configManager = new ConfigManager(this);
            if (!configManager.loadConfig()) {
                getLogger().severe("配置文件加载失败，插件将被禁用");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // 初始化其他组件
            releaseChecker = new GitHubReleaseChecker(configManager, getLogger());
            fileDownloader = new FileDownloader(configManager, getLogger(), getDataFolder());
            pluginLoaderUtil = new PluginLoaderUtil(configManager, getLogger());

            // 异步检查和下载最新版本
            checkAndDownloadLatestVersion();

        } catch (Exception e) {
            getLogger().severe("插件启动时发生错误: " + e.getMessage());
            e.printStackTrace();

            if (configManager != null && configManager.shouldShutdownOnFailure()) {
                getLogger().severe("根据配置，服务器将关闭");
                shutdownServer();
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AzuraBedWars Loader 正在关闭...");

        // 清理临时文件
        if (fileDownloader != null) {
            fileDownloader.cleanupTempFiles();
        }

        getLogger().info("AzuraBedWars Loader 已关闭");
    }

    /**
     * 检查并下载最新版本
     */
    private void checkAndDownloadLatestVersion() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    getLogger().info("正在检查最新版本...");

                    // 检查最新版本
                    GitHubReleaseChecker.ReleaseInfo latestRelease = releaseChecker.getLatestRelease();
                    if (latestRelease == null) {
                        getLogger().severe("无法获取最新版本信息");
                        handleDownloadFailure("无法获取版本信息");
                        return;
                    }

                    getLogger().info("发现最新版本: " + latestRelease.getTagName());
                    LoggerUtil.verbose("版本名称: " + latestRelease.getName());
                    LoggerUtil.verbose("发布时间: " + latestRelease.getPublishedAt());
                    LoggerUtil.verbose("是否为预发布: " + latestRelease.isPrerelease());

                    // 检查是否需要下载
                    if (!shouldDownloadVersion(latestRelease)) {
                        getLogger().info("当前版本已是最新，无需下载");
                        loadExistingPlugin();
                        return;
                    }

                    // 下载最新版本
                    downloadAndLoadPlugin(latestRelease);

                } catch (Exception e) {
                    getLogger().severe("检查版本时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    handleDownloadFailure("版本检查失败: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    /**
     * 检查是否需要下载版本
     */
    private boolean shouldDownloadVersion(GitHubReleaseChecker.ReleaseInfo release) {
        // 检查本地是否已存在该版本
        File pluginDir = getDataFolder();
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        // 查找现有的插件文件
        File[] existingFiles = pluginDir.listFiles((dir, name) ->
                name.startsWith(configManager.getPluginPrefix()) && name.endsWith(".jar"));

        if (existingFiles == null || existingFiles.length == 0) {
            getLogger().info("未找到现有插件文件，需要下载");
            return true;
        }

        // 检查版本文件
        File versionFile = new File(pluginDir, configManager.getVersionFileName());
        if (!versionFile.exists()) {
            getLogger().info("版本文件不存在，需要下载");
            return true;
        }

        try {
            String currentVersion = java.nio.file.Files.readString(versionFile.toPath()).trim();
            if (!release.getTagName().equals(currentVersion)) {
                getLogger().info("发现新版本 (当前: " + currentVersion + ", 最新: " + release.getTagName() + ")");
                return true;
            }
        } catch (Exception e) {
            getLogger().warning("读取版本文件失败: " + e.getMessage());
            return true;
        }

        return false;
    }

    /**
     * 下载并加载插件
     */
    private void downloadAndLoadPlugin(GitHubReleaseChecker.ReleaseInfo release) {
        try {
            String downloadUrl = release.getDownloadUrl();
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                getLogger().severe("无法找到下载链接");
                handleDownloadFailure("下载链接不存在");
                return;
            }

            getLogger().info("开始下载: " + release.getName());
            LoggerUtil.verbose("下载链接: " + downloadUrl);

            // 准备下载路径
            File pluginDir = getDataFolder();
            String fileName = configManager.getPluginPrefix() + "-" + release.getTagName() + ".jar";

            // 清理旧版本文件
            cleanupOldVersions(pluginDir);

            // 下载文件
            CompletableFuture<File> downloadFuture = CompletableFuture.supplyAsync(() -> fileDownloader.downloadFile(release));

            // 等待下载完成并在主线程中处理结果
            downloadFuture.whenComplete((downloadedFile, throwable) -> {
                LoggerUtil.verbose("下载回调被触发");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        LoggerUtil.verbose("进入主线程回调处理");

                        if (throwable != null) {
                            getLogger().severe("下载过程中发生异常: " + throwable.getMessage());
                            throwable.printStackTrace();
                            handleDownloadFailure("下载异常: " + throwable.getMessage());
                            return;
                        }

                        if (downloadedFile == null) {
                            getLogger().severe("文件下载失败");
                            handleDownloadFailure("文件下载失败");
                            return;
                        }

                        LoggerUtil.verbose("下载文件路径: " + downloadedFile.getAbsolutePath());
                        LoggerUtil.verbose("文件是否存在: " + downloadedFile.exists());

                        // 保存版本信息
                        saveVersionInfo(release.getTagName());

                        // 加载插件
                        LoggerUtil.verbose("准备加载插件");
                        loadDownloadedPlugin(downloadedFile);
                    }
                }.runTask(AzuraBedWarsLoader.this);
            });

        } catch (Exception e) {
            getLogger().severe("下载插件时发生错误: " + e.getMessage());
            e.printStackTrace();
            handleDownloadFailure("下载错误: " + e.getMessage());
        }
    }

    /**
     * 清理旧版本文件
     */
    private void cleanupOldVersions(File pluginDir) {
        try {
            File[] oldFiles = pluginDir.listFiles((dir, name) ->
                    name.startsWith(configManager.getPluginPrefix()) && name.endsWith(".jar"));

            if (oldFiles != null) {
                for (File oldFile : oldFiles) {
                    if (oldFile.delete()) {
                        getLogger().info("已删除旧版本文件: " + oldFile.getName());
                    } else {
                        getLogger().warning("无法删除旧版本文件: " + oldFile.getName());
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("清理旧版本时发生错误: " + e.getMessage());
        }
    }

    /**
     * 保存版本信息
     */
    private void saveVersionInfo(String version) {
        try {
            File versionFile = new File(getDataFolder(), configManager.getVersionFileName());
            java.nio.file.Files.writeString(versionFile.toPath(), version);
            LoggerUtil.verbose("版本信息已保存: " + version);
        } catch (Exception e) {
            getLogger().warning("保存版本信息失败: " + e.getMessage());
        }
    }

    /**
     * 加载下载的插件
     */
    private void loadDownloadedPlugin(File pluginFile) {
        try {
            LoggerUtil.verbose("loadDownloadedPlugin 方法被调用");
            LoggerUtil.verbose("插件文件: " + pluginFile.getAbsolutePath());
            LoggerUtil.verbose("文件存在: " + pluginFile.exists());
            LoggerUtil.verbose("文件大小: " + pluginFile.length() + " bytes");

            getLogger().info("正在加载插件: " + pluginFile.getName());

            LoggerUtil.verbose("调用 pluginLoader.loadPlugin");
            boolean success = pluginLoaderUtil.loadPlugin(pluginFile);
            LoggerUtil.verbose("pluginLoader.loadPlugin 返回: " + success);

            if (success) {
                getLogger().info("插件加载成功!");
                getLogger().info(pluginLoaderUtil.getPluginInfo());
                LoggerUtil.verbose("插件已成功加载并启用");
            } else {
                getLogger().severe("插件加载失败");
                handleDownloadFailure("插件加载失败");
            }

        } catch (Exception e) {
            getLogger().severe("加载插件时发生错误: " + e.getMessage());
            e.printStackTrace();
            handleDownloadFailure("插件加载错误: " + e.getMessage());
        }
    }

    /**
     * 加载现有插件
     */
    private void loadExistingPlugin() {
        try {
            File pluginDir = getDataFolder();
            File[] existingFiles = pluginDir.listFiles((dir, name) ->
                    name.startsWith(configManager.getPluginPrefix()) && name.endsWith(".jar"));

            if (existingFiles != null && existingFiles.length > 0) {
                File pluginFile = existingFiles[0]; // 取第一个匹配的文件
                getLogger().info("加载现有插件: " + pluginFile.getName());

                boolean success = pluginLoaderUtil.loadPlugin(pluginFile);
                if (success) {
                    getLogger().info("现有插件加载成功!");
                    getLogger().info(pluginLoaderUtil.getPluginInfo());
                } else {
                    getLogger().severe("现有插件加载失败");
                    handleDownloadFailure("现有插件加载失败");
                }
            } else {
                getLogger().warning("未找到现有插件文件");
            }

        } catch (Exception e) {
            getLogger().severe("加载现有插件时发生错误: " + e.getMessage());
            e.printStackTrace();
            handleDownloadFailure("现有插件加载错误: " + e.getMessage());
        }
    }

    /**
     * 处理下载失败
     */
    private void handleDownloadFailure(String reason) {
        getLogger().severe("下载失败: " + reason);

        if (configManager.shouldShutdownOnFailure()) {
            getLogger().severe("根据配置，服务器将关闭");
            shutdownServer();
        } else {
            getLogger().warning("下载失败，但服务器将继续运行");
        }
    }

    /**
     * 关闭服务器
     */
    private void shutdownServer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("正在关闭服务器...");
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(this, 20L); // 延迟1秒关闭
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GitHubReleaseChecker getReleaseChecker() {
        return releaseChecker;
    }

    public FileDownloader getFileDownloader() {
        return fileDownloader;
    }

    public PluginLoaderUtil getPluginLoaderUtil() {
        return pluginLoaderUtil;
    }
}
