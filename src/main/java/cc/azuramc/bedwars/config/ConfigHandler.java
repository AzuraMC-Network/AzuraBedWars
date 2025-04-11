package cc.azuramc.bedwars.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;

/**
 * 配置处理器
 * <p>
 * 负责单个配置文件的加载和保存
 * </p>
 * 
 * @param <T> 配置对象类型
 */
public class ConfigHandler<T> {
    private final File file;
    private final Gson gson;
    private final Class<T> clazz;

    /**
     * 创建一个配置处理器
     * 
     * @param file 配置文件
     * @param clazz 配置对象类型
     */
    public ConfigHandler(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * 加载配置
     * 
     * @param defaultInstance 默认实例
     * @return 加载的配置对象
     */
    public T load(T defaultInstance) {
        if (!file.exists()) {
            save(defaultInstance);
            return defaultInstance;
        }
        
        try (Reader reader = new FileReader(file)) {
            T loadedConfig = gson.fromJson(reader, clazz);
            return loadedConfig != null ? loadedConfig : defaultInstance;
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
            return defaultInstance;
        }
    }

    /**
     * 保存配置
     * 
     * @param instance 要保存的配置对象
     */
    public void save(Object instance) {
        try {
            // 确保父目录存在
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(instance, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
