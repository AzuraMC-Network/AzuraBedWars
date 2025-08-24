package cc.azuramc.bedwars.loader.download;

import cc.azuramc.bedwars.loader.config.ConfigManager;
import cc.azuramc.bedwars.loader.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author An5w1r@163.com
 */
public class GitHubReleaseChecker {
    private final ConfigManager configManager;
    private final Logger logger;
    private final Gson gson;

    public GitHubReleaseChecker(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
        this.gson = new Gson();
    }

    /**
     * 获取最新的Release信息
     *
     * @return ReleaseInfo对象，如果获取失败返回null
     */
    public ReleaseInfo getLatestRelease() {
        try {
            String apiUrl;
            if (configManager.isIncludePrerelease()) {
                // 如果包含预发布版本，获取所有releases并找到最新的
                apiUrl = configManager.getReleasesApiUrl();
                LoggerUtil.verbose("正在检查最新版本（包含预发布版本）...");
            } else {
                // 只获取最新的正式版本
                apiUrl = configManager.getLatestReleaseApiUrl();
                LoggerUtil.verbose("正在检查最新正式版本...");
            }

            String jsonResponse = makeHttpRequest(apiUrl);
            if (jsonResponse == null) {
                return null;
            }

            return parseReleaseInfo(jsonResponse);

        } catch (Exception e) {
            LoggerUtil.error("获取最新版本信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送HTTP请求
     */
    private String makeHttpRequest(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            // 设置请求属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(configManager.getConnectTimeout() * 1000);
            connection.setReadTimeout(configManager.getReadTimeout() * 1000);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "AzuraBedWars-Loader/1.0");

            int responseCode = connection.getResponseCode();
            LoggerUtil.verbose("GitHub API 响应码: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                return response.toString();
            } else {
                LoggerUtil.error("GitHub API 请求失败，响应码: " + responseCode);
                // 尝试读取错误信息
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    LoggerUtil.error("错误详情: " + errorResponse.toString());
                } catch (Exception ignored) {
                }
                return null;
            }

        } catch (IOException e) {
            LoggerUtil.error("网络请求失败: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 解析Release信息
     */
    private ReleaseInfo parseReleaseInfo(String jsonResponse) {
        try {
            if (configManager.isIncludePrerelease()) {
                // 解析releases数组，找到最新的release
                JsonArray releases = gson.fromJson(jsonResponse, JsonArray.class);
                if (releases.isEmpty()) {
                    logger.warning("没有找到任何release");
                    return null;
                }

                // 获取第一个release（最新的）
                JsonObject latestRelease = releases.get(0).getAsJsonObject();
                return parseReleaseObject(latestRelease);
            } else {
                // 解析单个latest release
                JsonObject release = gson.fromJson(jsonResponse, JsonObject.class);
                return parseReleaseObject(release);
            }
        } catch (Exception e) {
            LoggerUtil.error("解析Release信息时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析单个Release对象
     */
    private ReleaseInfo parseReleaseObject(JsonObject release) {
        String tagName = release.get("tag_name").getAsString();
        String name = release.get("name").getAsString();
        boolean prerelease = release.get("prerelease").getAsBoolean();
        String publishedAt = release.get("published_at").getAsString();

        LoggerUtil.verbose("找到版本: " + tagName + " (" + name + ")");
        LoggerUtil.verbose("是否为预发布版本: " + prerelease);
        LoggerUtil.verbose("发布时间: " + publishedAt);

        // 查找jar文件的下载链接
        JsonArray assets = release.getAsJsonArray("assets");
        String downloadUrl = null;
        String fileName = null;
        long fileSize = 0;

        for (JsonElement assetElement : assets) {
            JsonObject asset = assetElement.getAsJsonObject();
            String assetName = asset.get("name").getAsString();

            // 查找包含插件前缀的jar文件
            if (assetName.toLowerCase().contains(configManager.getPluginPrefix().toLowerCase())
                    && assetName.toLowerCase().endsWith(".jar")) {
                downloadUrl = asset.get("browser_download_url").getAsString();
                fileName = assetName;
                fileSize = asset.get("size").getAsLong();
                LoggerUtil.verbose("找到插件文件: " + fileName + " (" + fileSize + " 字节)");
                break;
            }
        }

        if (downloadUrl == null) {
            logger.warning("在Release " + tagName + " 中没有找到合适的jar文件");
            return null;
        }

        return new ReleaseInfo(tagName, name, downloadUrl, fileName, fileSize, prerelease, publishedAt);
    }

    /**
     * Release信息数据类
     */
    public static class ReleaseInfo {
        private final String tagName;
        private final String name;
        private final String downloadUrl;
        private final String fileName;
        private final long fileSize;
        private final boolean prerelease;
        private final String publishedAt;

        public ReleaseInfo(String tagName, String name, String downloadUrl,
                           String fileName, long fileSize, boolean prerelease, String publishedAt) {
            this.tagName = tagName;
            this.name = name;
            this.downloadUrl = downloadUrl;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.prerelease = prerelease;
            this.publishedAt = publishedAt;
        }

        public String getTagName() {
            return tagName;
        }

        public String getName() {
            return name;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public boolean isPrerelease() {
            return prerelease;
        }

        public String getPublishedAt() {
            return publishedAt;
        }

        public long getAssetSize() {
            return fileSize;
        }

        @Override
        public String toString() {
            return String.format("Release{tag='%s', name='%s', file='%s', size=%d, prerelease=%s}",
                    tagName, name, fileName, fileSize, prerelease);
        }
    }
}
