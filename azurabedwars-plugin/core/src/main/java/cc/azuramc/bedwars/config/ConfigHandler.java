package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.migrate.ConfigMigrator;
import cc.azuramc.bedwars.config.yaml.YamlDeserializer;
import cc.azuramc.bedwars.config.yaml.YamlSerializer;
import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * @author an5w1r@163.com
 */
public class ConfigHandler<T> {

    private final File file;
    private final Class<T> clazz;
    private final String configId;
    private final File configDir;

    private final YamlSerializer serializer = new YamlSerializer();
    private final YamlDeserializer deserializer = new YamlDeserializer();
    private final ConfigMigrator migrator = new ConfigMigrator();

    public ConfigHandler(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
        this.configDir = file.getParentFile();

        String fileName = file.getName();
        this.configId = fileName.endsWith(".yml")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;
    }

    public T load(T defaultInstance) {
        T migratedConfig = migrator.migrateIfNeeded(configDir, configId, clazz);
        if (migratedConfig != null) {
            save(migratedConfig);
            return migratedConfig;
        }

        if (!file.exists()) {
            save(defaultInstance);
            return defaultInstance;
        }

        return loadFromYaml(defaultInstance);
    }

    private T loadFromYaml(T defaultInstance) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            T instance = clazz.getDeclaredConstructor().newInstance();
            deserializer.deserialize(yaml, instance);
            return instance;
        } catch (Exception e) {
            LoggerUtil.error("加载配置文件失败: " + file.getAbsolutePath());
            e.printStackTrace();
            return defaultInstance;
        }
    }

    public void save(Object instance) {
        try {
            ensureParentDirectoryExists();

            String yamlContent = serializer.serialize(instance);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(yamlContent);
            }
        } catch (Exception e) {
            LoggerUtil.error("保存配置文件失败: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private void ensureParentDirectoryExists() {
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (!created) {
                LoggerUtil.error("无法创建配置文件目录: " + configDir.getAbsolutePath());
            }
        }
    }
}
