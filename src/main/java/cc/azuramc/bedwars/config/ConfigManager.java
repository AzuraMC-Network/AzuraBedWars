package cc.azuramc.bedwars.config;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器
 * <p>
 * 负责管理所有配置对象的加载和保存
 * </p>
 */
public class ConfigManager {
    @Getter
    private final File configDir;
    private final ConcurrentHashMap<String, ConfigHandler<?>> configHandlers;
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
     * @param id 配置ID
     * @param handler 配置处理器
     * @param defaultInstance 默认实例
     */
    public <T> void registerConfig(String id, ConfigHandler<T> handler, T defaultInstance) {
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
     * @param id 配置ID
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
        ConfigHandler<?> handler = configHandlers.get(id);
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
        ConfigHandler<?> handler = configHandlers.get(id);
        if (handler != null) {
            Object config = handler.load(null);
            if (config != null) {
                configObjects.put(id, config);
            }
        }
    }
} 