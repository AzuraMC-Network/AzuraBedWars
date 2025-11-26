package cc.azuramc.bedwars.config;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器
 * 负责管理所有配置对象的加载和保存
 * 支持多种配置格式（JSON、YAML等）
 *
 * @author an5w1r@163.com
 */
public class ConfigManager {
    @Getter
    private final File configDir;
    private final ConcurrentHashMap<String, IConfigHandler<?>> configHandlers;
    private final ConcurrentHashMap<String, Object> configObjects;

    /**
     * 创建一个配置管理器
     *
     * @param plugin 插件实例
     */
    public ConfigManager(Plugin plugin) {
        this.configDir = new File(plugin.getDataFolder(), "config");
        this.configHandlers = new ConcurrentHashMap<>();
        this.configObjects = new ConcurrentHashMap<>();

        // 确保配置目录存在
        if (!configDir.exists()) {
            boolean created = configDir.mkdirs();
            if (!created) {
                plugin.getLogger().severe("无法创建配置目录：" + configDir.getAbsolutePath());
            }
        }
    }

    /**
     * 注册配置对象
     *
     * @param id              配置ID
     * @param handler         配置处理器（支持JSON、YAML等格式）
     * @param defaultInstance 默认实例
     */
    public <T> void registerConfig(String id, IConfigHandler<T> handler, T defaultInstance) {
        configHandlers.put(id, handler);
        // 加载配置对象
        T config = handler.load(defaultInstance);
        if (config != null) {
            configObjects.put(id, config);
        }
    }

    /**
     * 获取配置对象
     *
     * @param id    配置ID
     * @param clazz 配置对象类型
     * @return 配置对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String id, Class<T> clazz) {
        Object config = configObjects.get(id);
        if (clazz.isInstance(config)) {
            return (T) config;
        }
        return null;
    }

    /**
     * 保存所有配置
     */
    public void saveAll() {
        configHandlers.forEach((id, handler) -> {
            Object config = configObjects.get(id);
            if (config != null) {
                handler.save(config);
            }
        });
    }

    /**
     * 保存指定配置
     *
     * @param id 配置ID
     */
    public void saveConfig(String id) {
        IConfigHandler<?> handler = configHandlers.get(id);
        Object config = configObjects.get(id);
        if (handler != null && config != null) {
            handler.save(config);
        }
    }

    /**
     * 重新加载所有配置
     */
    public void reloadAll() {
        configHandlers.forEach((id, handler) -> {
            Object config = handler.load(null);
            if (config != null) {
                configObjects.put(id, config);
            }
        });
    }

    /**
     * 重新加载指定配置
     *
     * @param id 配置ID
     */
    public void reloadConfig(String id) {
        IConfigHandler<?> handler = configHandlers.get(id);
        if (handler != null) {
            Object config = handler.load(null);
            if (config != null) {
                configObjects.put(id, config);
            }
        }
    }

    /**
     * 将指定配置转换为JSON字符串
     * 用于云存储等场景
     *
     * @param id 配置ID
     * @return JSON字符串，如果配置不存在返回null
     */
    public String configToJson(String id) {
        IConfigHandler<?> handler = configHandlers.get(id);
        Object config = configObjects.get(id);
        if (handler != null && config != null) {
            return handler.toJson(config);
        }
        return null;
    }

    /**
     * 从JSON字符串加载配置
     * 用于从云存储加载配置
     *
     * @param id   配置ID
     * @param json JSON字符串
     */
    public void configFromJson(String id, String json) {
        IConfigHandler<?> handler = configHandlers.get(id);
        if (handler != null) {
            Object config = handler.fromJson(json);
            if (config != null) {
                configObjects.put(id, config);
                // 保存到本地文件
                handler.save(config);
            }
        }
    }
}
