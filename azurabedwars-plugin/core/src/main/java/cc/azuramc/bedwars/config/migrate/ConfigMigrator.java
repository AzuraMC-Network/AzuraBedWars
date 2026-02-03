package cc.azuramc.bedwars.config.migrate;

import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author an5w1r@163.com
 */
public class ConfigMigrator {

    private static final String BACKUP_DIR_NAME = "backupjson";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public <T> T migrateIfNeeded(File configDir, String configId, Class<T> clazz) {
        File jsonFile = new File(configDir, configId + ".json");

        if (!jsonFile.exists()) {
            return null;
        }

        LoggerUtil.info("检测到旧的 JSON 配置文件: " + jsonFile.getName() + "，正在迁移...");

        try {
            T config = loadFromJson(jsonFile, clazz);

            if (config != null) {
                backupJsonFile(configDir, jsonFile);
                LoggerUtil.info("JSON 配置迁移成功: " + configId);
            }

            return config;
        } catch (Exception e) {
            LoggerUtil.error("迁移 JSON 配置文件失败: " + jsonFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    private <T> T loadFromJson(File jsonFile, Class<T> clazz) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        }
    }

    private void backupJsonFile(File configDir, File jsonFile) throws IOException {
        File backupDir = new File(configDir, BACKUP_DIR_NAME);

        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                LoggerUtil.warn("无法创建备份目录: " + backupDir.getAbsolutePath());
                return;
            }
        }

        File backupFile = new File(backupDir, jsonFile.getName());

        if (backupFile.exists()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String name = jsonFile.getName();
            int dotIndex = name.lastIndexOf('.');
            String newName = name.substring(0, dotIndex) + "_" + timestamp + name.substring(dotIndex);
            backupFile = new File(backupDir, newName);
        }

        Files.move(jsonFile.toPath(), backupFile.toPath());
        LoggerUtil.info("已备份旧配置文件到: " + backupFile.getPath());
    }
}
