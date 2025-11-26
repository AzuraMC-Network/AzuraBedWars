package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.util.LoggerUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * YAML配置文件处理器
 * 对外暴露YAML格式（用户友好），内部使用Java对象（便于代码操作）
 * 同时保留JSON序列化能力（用于云存储）
 *
 * @param <T> 配置类的类型
 */
public class YamlConfigHandler<T> implements IConfigHandler<T> {

    private final File file;
    private final Class<T> clazz;
    private final YamlConverter converter;

    /**
     * 构造函数
     *
     * @param file  配置文件
     * @param clazz 配置类的Class对象
     */
    public YamlConfigHandler(File file, Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
        this.converter = new YamlConverter();
    }

    /**
     * 加载YAML配置文件
     * 如果文件不存在，则使用默认实例创建文件
     *
     * @param defaultInstance 默认配置实例
     * @return 加载的配置对象，如果加载失败则返回默认实例
     */
    @Override
    public T load(T defaultInstance) {
        // 如果文件不存在，创建默认配置文件
        if (!file.exists()) {
            save(defaultInstance);
            LoggerUtil.info("创建默认配置文件: " + file.getName());
            return defaultInstance;
        }

        try {
            // 加载YAML文件
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(file);

            // 将YAML转换为Java对象
            T loadedConfig = converter.fromYaml(yaml, clazz);

            if (loadedConfig != null) {
                LoggerUtil.info("成功加载配置文件: " + file.getName());
                return loadedConfig;
            } else {
                LoggerUtil.warn("配置文件加载为空，使用默认配置: " + file.getName());
                return defaultInstance;
            }

        } catch (IOException e) {
            LoggerUtil.error("读取配置文件失败: " + file.getName());
            e.printStackTrace();
            return defaultInstance;
        } catch (InvalidConfigurationException e) {
            LoggerUtil.error("配置文件格式错误: " + file.getName());
            e.printStackTrace();
            return defaultInstance;
        } catch (Exception e) {
            LoggerUtil.error("解析配置文件时发生错误: " + file.getName());
            e.printStackTrace();
            return defaultInstance;
        }
    }

    /**
     * 保存配置到YAML文件（带字段注释）
     *
     * @param instance 要保存的配置实例
     */
    @Override
    public void save(Object instance) {
        try {
            // 确保父目录存在
            if (!file.getParentFile().exists()) {
                boolean created = file.getParentFile().mkdirs();
                if (!created) {
                    LoggerUtil.error("无法创建配置文件目录: " + file.getParentFile().getAbsolutePath());
                    return;
                }
            }

            // 将Java对象转换为YAML
            YamlConfiguration yaml = converter.toYaml(instance);

            // 使用带注释的方式保存YAML文件
            // 会自动读取配置类字段上的 @ConfigComment 注解并添加注释
            CommentedYamlWriter.save(yaml, instance, file, generateHeader());

            LoggerUtil.info("成功保存配置文件: " + file.getName());

        } catch (IOException e) {
            LoggerUtil.error("保存配置文件失败: " + file.getName());
            e.printStackTrace();
        } catch (Exception e) {
            LoggerUtil.error("保存配置文件时发生错误: " + file.getName());
            e.printStackTrace();
        }
    }

    /**
     * 将配置对象序列化为JSON字符串
     * 用于云存储等场景
     *
     * @param instance 配置实例
     * @return JSON字符串
     */
    @Override
    public String toJson(Object instance) {
        return converter.toJson(instance);
    }

    /**
     * 从JSON字符串反序列化为配置对象
     * 用于从云存储加载配置
     *
     * @param json JSON字符串
     * @return 配置对象
     */
    @Override
    public T fromJson(String json) {
        return converter.fromJson(json, clazz);
    }

    /**
     * 生成配置文件头部注释
     *
     * @return 头部注释字符串
     */
    private String generateHeader() {
        return "\n" +
                "AzuraBedWars 配置文件\n" +
                "配置文件使用 YAML 格式，方便手动编辑\n" +
                "注意：修改配置后需要使用 /bw reload 命令重新加载\n" +
                "警告：请勿删除或修改配置项的键名，否则可能导致插件无法正常工作\n";
    }

    /**
     * 获取配置文件
     *
     * @return 配置文件
     */
    public File getFile() {
        return file;
    }

    /**
     * 获取配置类的Class对象
     *
     * @return Class对象
     */
    public Class<T> getConfigClass() {
        return clazz;
    }
}
