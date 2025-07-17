package cc.azuramc.bedwars.config;

import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 配置工厂
 * 负责创建和管理配置对象
 * @author an5w1r@163.com
 */
public class ConfigFactory {
    private final Map<String, Supplier<?>> configSuppliers;
    private final Map<String, Object> defaultInstances;
    
    @Getter
    private static ConfigFactory instance;
    
    /**
     * 创建一个配置工厂
     */
    public ConfigFactory() {
        this.configSuppliers = new HashMap<>();
        this.defaultInstances = new HashMap<>();
        instance = this;
    }
    
    /**
     * 注册一个配置对象供应商
     * 
     * @param id 配置对象ID
     * @param supplier 配置对象供应商
     * @param <T> 配置对象类型
     */
    public <T> void registerSupplier(String id, Supplier<T> supplier) {
        configSuppliers.put(id, supplier);
        defaultInstances.put(id, supplier.get());
    }
    
    /**
     * 创建一个配置对象
     * 
     * @param id 配置对象ID
     * @return 创建的配置对象，如果供应商不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T createConfig(String id) {
        Supplier<?> supplier = configSuppliers.get(id);
        
        if (supplier != null) {
            return (T) supplier.get();
        }
        
        return null;
    }
    
    /**
     * 获取默认实例
     * 
     * @param id 配置对象ID
     * @return 默认实例，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getDefaultInstance(String id) {
        return (T) defaultInstances.get(id);
    }
    
    /**
     * 初始化默认配置
     * 
     * @param configManager 配置管理器
     */
    @SuppressWarnings("unchecked")
    public void initializeDefaults(ConfigManager configManager) {
        for (Map.Entry<String, Supplier<?>> entry : configSuppliers.entrySet()) {
            String id = entry.getKey();
            Object defaultInstance = defaultInstances.get(id);
            
            if (defaultInstance != null) {
                Class<?> configClass = defaultInstance.getClass();
                ConfigHandler<Object> handler = new ConfigHandler<>(
                    new File(configManager.getConfigDir(), id + ".json"),
                    (Class<Object>) configClass
                );
                configManager.registerConfig(id, handler, defaultInstance);
            }
        }
    }
} 