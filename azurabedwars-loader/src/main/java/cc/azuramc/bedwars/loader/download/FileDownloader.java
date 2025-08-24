package cc.azuramc.bedwars.loader.download;

import cc.azuramc.bedwars.loader.config.ConfigManager;
import cc.azuramc.bedwars.loader.util.LoggerUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * @author An5w1r@163.com
 */
public class FileDownloader {
    private final ConfigManager configManager;
    private final Logger logger;
    private final File pluginDataFolder;

    private long lastProgressBytes = 0;
    private double lastProgressPercentage = 0.0;

    public FileDownloader(ConfigManager configManager, Logger logger, File pluginDataFolder) {
        this.configManager = configManager;
        this.logger = logger;
        this.pluginDataFolder = pluginDataFolder;
    }

    /**
     * 验证文件名是否安全，防止路径遍历攻击
     *
     * @param fileName 要验证的文件名
     * @return 如果文件名安全返回true，否则返回false
     */
    private boolean isSafeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            LoggerUtil.error("文件名为空或null");
            return false;
        }

        // 移除首尾空白字符
        fileName = fileName.trim();

        // 检查文件名长度（防止过长文件名攻击）
        if (fileName.length() > 255) {
            LoggerUtil.error("文件名过长: " + fileName.length() + " 字符");
            return false;
        }

        // 禁止路径遍历字符
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            LoggerUtil.error("文件名包含危险的路径字符: " + fileName);
            return false;
        }

        // 禁止控制字符和特殊字符
        if (fileName.matches(".*[\\x00-\\x1F\\x7F<>:\"|?*].*")) {
            LoggerUtil.error("文件名包含非法字符: " + fileName);
            return false;
        }

        // 要求以.jar结尾（符合业务逻辑）
        if (!fileName.toLowerCase().endsWith(".jar")) {
            LoggerUtil.error("文件名必须以.jar结尾: " + fileName);
            return false;
        }

        // 检查是否只包含安全的文件名字符（字母、数字、下划线、短横线、点）
        if (!fileName.matches("^[\\w\\-.]+\\.jar$")) {
            LoggerUtil.error("文件名包含不安全的字符: " + fileName);
            return false;
        }

        LoggerUtil.verbose("文件名安全验证通过: " + fileName);
        return true;
    }

    /**
     * 下载文件
     *
     * @param releaseInfo 版本信息
     * @return 下载成功返回文件路径，失败返回null
     */
    public File downloadFile(GitHubReleaseChecker.ReleaseInfo releaseInfo) {
        String downloadUrl = releaseInfo.getDownloadUrl();
        String fileName = releaseInfo.getFileName();
        long expectedSize = releaseInfo.getFileSize();

        // 文件名安全性验证
        if (!isSafeFileName(fileName)) {
            String errorMsg = "检测到不安全的文件名，可能存在路径遍历攻击: " + fileName;
            LoggerUtil.error(errorMsg);
            LoggerUtil.error("下载来源: " + downloadUrl);
            LoggerUtil.error("安全检查失败，拒绝下载操作");
            throw new SecurityException(errorMsg);
        }

        lastProgressBytes = 0;
        lastProgressPercentage = 0.0;

        File targetFile = new File(pluginDataFolder, fileName);
        File tempFile = new File(pluginDataFolder, fileName + configManager.getTempSuffix());

        LoggerUtil.info("开始下载: " + fileName);
        LoggerUtil.info("下载地址: " + downloadUrl);
        LoggerUtil.info("文件大小: " + formatFileSize(expectedSize));
        LoggerUtil.info("保存路径: " + targetFile.getAbsolutePath());

        cleanupTempFiles();

        boolean downloadSuccess = false;
        int retryCount = configManager.getRetryCount();

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            if (attempt > 1) {
                LoggerUtil.info("第 " + attempt + " 次重试下载...");
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        LoggerUtil.verbose("已删除不完整的临时文件");
                    }
                }
            }

            try {
                if (performDownload(downloadUrl, tempFile, expectedSize)) {
                    downloadSuccess = true;
                    break;
                }
            } catch (Exception e) {
                logger.warning("第 " + attempt + " 次下载尝试失败: " + e.getMessage());
                if (attempt == retryCount) {
                    LoggerUtil.error("所有下载尝试都失败了");
                }
            }

            if (attempt < retryCount) {
                int delaySeconds = configManager.getRetryDelay();
                LoggerUtil.info("等待 " + delaySeconds + " 秒后重试...");
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("重试等待被中断");
                    break;
                }
            }
        }

        if (!downloadSuccess) {
            LoggerUtil.error("下载失败，已尝试 " + retryCount + " 次");
            // 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        }

        // 验证下载的文件
        if (!validateDownloadedFile(tempFile, expectedSize)) {
            LoggerUtil.error("下载的文件验证失败");
            tempFile.delete();
            return null;
        }

        // 移动临时文件到最终位置
        try {
            // 如果目标文件已存在，先删除
            if (targetFile.exists()) {
                targetFile.delete();
                LoggerUtil.verbose("已删除旧版本文件: " + targetFile.getName());
            }

            // 移动临时文件
            Path tempPath = tempFile.toPath();
            Path targetPath = targetFile.toPath();
            Files.move(tempPath, targetPath);

            LoggerUtil.info("文件下载完成: " + targetFile.getName());
            return targetFile;

        } catch (IOException e) {
            LoggerUtil.error("移动临时文件时发生错误: " + e.getMessage());
            tempFile.delete();
            return null;
        }
    }

    /**
     * 执行实际的下载操作
     */
    private boolean performDownload(String downloadUrl, File tempFile, long expectedSize) {
        if (configManager.isNetworkOptimizationEnabled()) {
            setNetworkSystemProperties();

            // 测试网络连通性
            if (!testNetworkConnectivity(downloadUrl)) {
                logger.warning("网络连通性测试失败，但仍将尝试下载");
            }
        }

        // 检查是否启用多线程下载且线程数大于1
        if (configManager.isMultithreadedDownloadEnabled() && configManager.getThreadCount() > 1) {
            LoggerUtil.verbose("尝试使用多线程下载，文件大小: " + formatFileSize(expectedSize));
            return performMultithreadedDownload(downloadUrl, tempFile, expectedSize);
        } else {
            if (!configManager.isMultithreadedDownloadEnabled()) {
                LoggerUtil.verbose("多线程下载已禁用，使用单线程下载");
            } else {
                LoggerUtil.verbose("配置的线程数为 " + configManager.getThreadCount() + "，使用单线程下载");
            }
            return performSingleThreadedDownload(downloadUrl, tempFile, expectedSize);
        }
    }

    /**
     * 执行单线程下载
     */
    private boolean performSingleThreadedDownload(String downloadUrl, File tempFile, long expectedSize) {

        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();

            // 设置请求属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(configManager.getConnectTimeout() * 1000);
            connection.setReadTimeout(configManager.getReadTimeout() * 1000);
            connection.setRequestProperty("User-Agent", "AzuraBedWars-Loader/1.0");
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Accept-Encoding", "identity");

            // 禁用缓存以避免连接问题
            connection.setUseCaches(false);
            connection.setDoInput(true);

            LoggerUtil.verbose("正在连接到: " + downloadUrl);
            LoggerUtil.verbose("连接超时设置: " + configManager.getConnectTimeout() + "秒");
            LoggerUtil.verbose("读取超时设置: " + configManager.getReadTimeout() + "秒");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LoggerUtil.error("下载请求失败，响应码: " + responseCode);
                return false;
            }

            long contentLength = connection.getContentLengthLong();
            LoggerUtil.verbose("服务器返回文件大小: " + formatFileSize(contentLength));

            // 创建输入输出流
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream outputStream = new FileOutputStream(tempFile);
                 BufferedOutputStream bufferedOutput = new BufferedOutputStream(outputStream)) {

                byte[] buffer = new byte[configManager.getBufferSize()];
                long totalBytesRead = 0;
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutput.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 更新进度（基于下载量变化）
                    if (configManager.isShowProgress() && shouldUpdateProgress(totalBytesRead, expectedSize)) {
                        showProgress(totalBytesRead, expectedSize);
                    }
                }

                bufferedOutput.flush();

                // 下载完成时显示最终进度
                if (configManager.isShowProgress()) {
                    showProgress(totalBytesRead, expectedSize);
                    LoggerUtil.info("下载进度更新完成");
                }

                LoggerUtil.verbose("下载完成，总共下载: " + formatFileSize(totalBytesRead));
                return true;

            }

        } catch (java.net.SocketTimeoutException e) {
            LoggerUtil.error("下载超时: " + e.getMessage());
            LoggerUtil.info("提示: 可能是网络连接不稳定，请检查网络环境或增加超时时间");
            return false;
        } catch (java.net.ConnectException e) {
            LoggerUtil.error("连接失败: " + e.getMessage());
            LoggerUtil.info("提示: 无法连接到下载服务器，请检查网络连接");
            return false;
        } catch (java.net.UnknownHostException e) {
            LoggerUtil.error("域名解析失败: " + e.getMessage());
            LoggerUtil.info("提示: 无法解析GitHub域名，请检查DNS设置");
            return false;
        } catch (IOException e) {
            LoggerUtil.error("下载过程中发生IO错误: " + e.getMessage());
            LoggerUtil.info("错误类型: " + e.getClass().getSimpleName());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 判断是否应该更新进度显示
     * 基于下载量变化来控制更新频率，避免频繁输出
     */
    private boolean shouldUpdateProgress(long downloaded, long total) {
        if (total <= 0) {
            // 对于未知大小的文件，每下载1MB更新一次
            return downloaded - lastProgressBytes >= 1024 * 1024;
        }

        double currentPercentage = (double) downloaded / total * 100;

        // 下载完成时必须更新
        if (downloaded >= total) {
            return true;
        }

        // 进度变化超过2%或下载量变化超过1MB时更新
        boolean percentageChanged = currentPercentage - lastProgressPercentage >= 2.0;
        boolean bytesChanged = downloaded - lastProgressBytes >= 1024 * 1024;

        if (percentageChanged || bytesChanged) {
            lastProgressPercentage = currentPercentage;
            lastProgressBytes = downloaded;
            return true;
        }

        return false;
    }

    /**
     * 显示下载进度
     * 使用LoggerUtil输出，避免控制台刷屏
     */
    private void showProgress(long downloaded, long total) {
        if (total <= 0) {
            LoggerUtil.info("下载中... " + formatFileSize(downloaded));
            return;
        }

        double percentage = (double) downloaded / total * 100;
        int progressBarLength = 20;
        int filledLength = (int) (progressBarLength * downloaded / total);

        StringBuilder progressBar = new StringBuilder();
        progressBar.append("[");

        for (int i = 0; i < progressBarLength; i++) {
            if (i < filledLength) {
                progressBar.append("=");
            } else if (i == filledLength) {
                progressBar.append(">");
            } else {
                progressBar.append(" ");
            }
        }

        progressBar.append(String.format("] %.1f%% (%s/%s)",
                percentage, formatFileSize(downloaded), formatFileSize(total)));

        LoggerUtil.info("下载进度: " + progressBar);
    }

    /**
     * 验证下载的文件
     */
    private boolean validateDownloadedFile(File file, long expectedSize) {
        if (!file.exists()) {
            LoggerUtil.error("下载的文件不存在");
            return false;
        }

        long actualSize = file.length();
        if (expectedSize > 0 && actualSize != expectedSize) {
            LoggerUtil.error(String.format("文件大小不匹配，期望: %s，实际: %s",
                    formatFileSize(expectedSize), formatFileSize(actualSize)));
            return false;
        }

        if (actualSize == 0) {
            LoggerUtil.error("下载的文件为空");
            return false;
        }

        LoggerUtil.verbose("文件验证通过，大小: " + formatFileSize(actualSize));
        return true;
    }

    /**
     * 清理临时文件
     */
    public void cleanupTempFiles() {
        try {
            File[] files = pluginDataFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(configManager.getTempSuffix())) {
                        if (file.delete()) {
                            LoggerUtil.verbose("已清理临时文件: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("清理临时文件时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试网络连通性
     */
    private boolean testNetworkConnectivity(String downloadUrl) {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection testConnection = (HttpURLConnection) url.openConnection();

            testConnection.setRequestMethod("HEAD");
            testConnection.setConnectTimeout(configManager.getConnectionTestTimeout() * 1000);
            testConnection.setReadTimeout(configManager.getConnectionTestTimeout() * 1000);
            testConnection.setRequestProperty("User-Agent", "AzuraBedWars-Loader/1.0");

            int responseCode = testConnection.getResponseCode();
            testConnection.disconnect();

            boolean isConnected = (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL);
            LoggerUtil.verbose("网络连通性测试结果: " + (isConnected ? "成功" : "失败") + " (响应码: " + responseCode + ")");

            return isConnected;
        } catch (Exception e) {
            LoggerUtil.verbose("网络连通性测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置Java系统网络属性以优化连接
     */
    private void setNetworkSystemProperties() {
        try {
            // 设置网络连接超时
            System.setProperty("sun.net.useExclusiveBind", "false");
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.net.preferIPv6Addresses", "false");

            // 设置HTTP连接池
            System.setProperty("http.maxConnections", "5");
            System.setProperty("http.keepAlive", "true");
            System.setProperty("http.maxRedirects", "3");

            // 设置socket超时
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(configManager.getConnectTimeout() * 1000));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(configManager.getReadTimeout() * 1000));

            LoggerUtil.verbose("已设置Java网络系统属性");
        } catch (Exception e) {
            logger.warning("设置网络系统属性时发生错误: " + e.getMessage());
        }
    }

    /**
     * 执行多线程下载
     */
    private boolean performMultithreadedDownload(String downloadUrl, File tempFile, long expectedSize) {
        int configuredThreadCount = configManager.getThreadCount();

        // 根据文件大小自动计算合理的线程数和分片大小
        // 最小分片大小为64KB，避免创建过多小分片
        long minChunkSize = 64 * 1024; // 64KB

        // 计算实际需要的线程数
        int actualThreadCount = (int) Math.min(configuredThreadCount, Math.max(1, expectedSize / minChunkSize));

        // 如果文件太小，使用单线程下载
        if (expectedSize < minChunkSize || actualThreadCount == 1) {
            LoggerUtil.verbose("文件较小或计算得出单线程最优，切换到单线程下载");
            return performSingleThreadedDownload(downloadUrl, tempFile, expectedSize);
        }

        // 根据文件大小和实际线程数计算每个线程的分片大小
        long chunkSize = expectedSize / actualThreadCount;

        LoggerUtil.verbose("启动 " + actualThreadCount + " 个下载线程，每个线程处理约 " + formatFileSize(chunkSize) + " 数据");
        LoggerUtil.verbose("文件总大小: " + formatFileSize(expectedSize) + "，配置线程数: " + configuredThreadCount + "，实际线程数: " + actualThreadCount);

        ExecutorService executor = Executors.newFixedThreadPool(actualThreadCount);
        CompletableFuture<Boolean>[] futures = new CompletableFuture[actualThreadCount];
        AtomicLong totalDownloaded = new AtomicLong(0);

        // 创建文件写入锁对象
        final Object fileLock = new Object();

        try {
            // 创建临时文件并预分配空间
            RandomAccessFile sharedFile = new RandomAccessFile(tempFile, "rw");
            sharedFile.setLength(expectedSize);

            // 启动下载线程
            for (int i = 0; i < actualThreadCount; i++) {
                final int threadIndex = i;
                final long startByte = i * chunkSize;
                // 最后一个线程处理剩余的所有字节
                final long endByte = (i == actualThreadCount - 1) ? expectedSize - 1 : startByte + chunkSize - 1;

                futures[i] = CompletableFuture.supplyAsync(() -> {
                    return downloadChunk(downloadUrl, sharedFile, startByte, endByte, threadIndex, totalDownloaded, expectedSize, fileLock);
                }, executor);
            }

            // 等待所有线程完成
            boolean allSuccess = true;
            for (CompletableFuture<Boolean> future : futures) {
                try {
                    if (!future.get(configManager.getReadTimeout(), TimeUnit.SECONDS)) {
                        allSuccess = false;
                    }
                } catch (Exception e) {
                    LoggerUtil.error("下载线程异常: " + e.getMessage());
                    allSuccess = false;
                }
            }

            // 关闭共享文件
            sharedFile.close();

            // 多线程下载完成时显示最终进度
            if (configManager.isShowProgress()) {
                LoggerUtil.info("多线程下载进度更新完成");
            }

            return allSuccess;

        } catch (Exception e) {
            LoggerUtil.error("多线程下载初始化失败: " + e.getMessage());
            return false;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 下载文件块
     */
    private boolean downloadChunk(String downloadUrl, RandomAccessFile sharedFile, long startByte, long endByte,
                                  int threadIndex, AtomicLong totalDownloaded, long expectedSize, Object fileLock) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();

            // 设置请求属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(configManager.getConnectTimeout() * 1000);
            connection.setReadTimeout(configManager.getReadTimeout() * 1000);
            connection.setRequestProperty("User-Agent", "AzuraBedWars-Loader/1.0");
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);

            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                LoggerUtil.error("线程 " + threadIndex + " 下载请求失败，响应码: " + responseCode);
                return false;
            }

            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {

                byte[] buffer = new byte[configManager.getBufferSize()];
                long bytesRead = 0;
                int len;

                while ((len = inputStream.read(buffer)) != -1 && bytesRead < (endByte - startByte + 1)) {
                    // 确保不超过块边界
                    int actualLen = (int) Math.min(len, (endByte - startByte + 1) - bytesRead);

                    // 同步写入文件 - 将seek和write操作放在同一个synchronized块中
                    synchronized (fileLock) {
                        sharedFile.seek(startByte + bytesRead);
                        sharedFile.write(buffer, 0, actualLen);
                        // 确保数据立即写入磁盘
                        sharedFile.getFD().sync();
                    }

                    bytesRead += actualLen;

                    // 更新总进度
                    long currentTotal = totalDownloaded.addAndGet(actualLen);

                    // 更新进度显示（基于下载量变化，仅主线程显示）
                    if (configManager.isShowProgress() && threadIndex == 0 && shouldUpdateProgress(currentTotal, expectedSize)) {
                        showProgress(currentTotal, expectedSize);
                    }
                }

                LoggerUtil.verbose("线程 " + threadIndex + " 完成下载: " + formatFileSize(bytesRead));
                return true;
            }

        } catch (Exception e) {
            LoggerUtil.error("线程 " + threadIndex + " 下载异常: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
