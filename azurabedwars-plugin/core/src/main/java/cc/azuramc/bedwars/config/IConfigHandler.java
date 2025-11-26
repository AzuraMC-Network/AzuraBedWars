package cc.azuramc.bedwars.config;

/**
 * 配置处理器接口
 * 定义配置文件的加载和保存操作
 *
 * @param <T> 配置类的类型
 */
public interface IConfigHandler<T> {

    /**
     * 加载配置文件
     * 如果文件不存在，则使用默认实例创建文件
     *
     * @param defaultInstance 默认配置实例
     * @return 加载的配置对象，如果加载失败则返回默认实例
     */
    T load(T defaultInstance);

    /**
     * 保存配置到文件
     *
     * @param instance 要保存的配置实例
     */
    void save(Object instance);

    /**
     * 将配置对象序列化为JSON字符串
     * 用于云存储等场景
     *
     * @param instance 配置实例
     * @return JSON字符串
     */
    String toJson(Object instance);

    /**
     * 从JSON字符串反序列化为配置对象
     * 用于从云存储加载配置
     *
     * @param json JSON字符串
     * @return 配置对象
     */
    T fromJson(String json);
}
