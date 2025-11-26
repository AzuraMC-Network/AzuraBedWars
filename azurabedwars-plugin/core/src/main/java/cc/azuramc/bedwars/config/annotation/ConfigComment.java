package cc.azuramc.bedwars.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置字段注释注解
 * 用于为YAML配置文件中的字段添加注释说明
 *
 * 使用示例：
 * <pre>
 * {@code
 * @Data
 * public class MyConfig {
 *     @ConfigComment("是否启用调试模式")
 *     private boolean debugMode = false;
 *
 *     @ConfigComment({
 *         "最大玩家数量",
 *         "设置为0表示无限制"
 *     })
 *     private int maxPlayers = 16;
 * }
 * }
 * </pre>
 *
 * @author AzuraBedWars Team
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigComment {

    /**
     * 注释内容
     * 支持多行注释，每个元素代表一行
     *
     * @return 注释文本数组
     */
    String[] value();
}
