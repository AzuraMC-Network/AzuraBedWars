package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 带注释的YAML文件写入工具
 * 通过反射读取配置类字段上的 @ConfigComment 注解，
 * 并在生成的YAML文件中添加相应的注释
 *
 * @author AzuraBedWars Team
 */
public class CommentedYamlWriter {

    private static final String COMMENT_PREFIX = "# ";
    private static final String INDENT = "  ";

    /**
     * 将带注释的YAML配置保存到文件
     *
     * @param yaml          YamlConfiguration对象
     * @param configObject  配置对象（用于提取注释）
     * @param file          目标文件
     * @param header        文件头部注释
     * @throws IOException 写入文件失败
     */
    public static void save(YamlConfiguration yaml, Object configObject, File file, String header) throws IOException {
        // 收集所有字段的注释
        Map<String, List<String>> comments = collectComments(configObject.getClass());

        // 生成带注释的YAML内容
        String content = generateYamlWithComments(yaml, comments, header);

        // 写入文件
        try (Writer writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    /**
     * 收集配置类中所有字段的注释
     *
     * @param clazz 配置类
     * @return 字段路径到注释的映射
     */
    private static Map<String, List<String>> collectComments(Class<?> clazz) {
        Map<String, List<String>> comments = new LinkedHashMap<>();
        collectCommentsRecursive(clazz, "", comments);
        return comments;
    }

    /**
     * 递归收集配置类及其嵌套类的注释
     *
     * @param clazz    当前类
     * @param prefix   字段路径前缀
     * @param comments 注释映射
     */
    private static void collectCommentsRecursive(Class<?> clazz, String prefix, Map<String, List<String>> comments) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // 跳过静态字段和合成字段
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            String fieldName = field.getName();
            String fieldPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            // 检查字段是否有 @ConfigComment 注解
            if (field.isAnnotationPresent(ConfigComment.class)) {
                ConfigComment comment = field.getAnnotation(ConfigComment.class);
                comments.put(fieldPath, Arrays.asList(comment.value()));
            }

            // 检查字段类型是否是嵌套配置类
            Class<?> fieldType = field.getType();
            if (isConfigClass(fieldType)) {
                // 递归处理嵌套类
                collectCommentsRecursive(fieldType, fieldPath, comments);
            }
        }
    }

    /**
     * 判断一个类是否是配置类（嵌套类）
     *
     * @param clazz 要检查的类
     * @return 是否为配置类
     */
    private static boolean isConfigClass(Class<?> clazz) {
        // 排除基本类型、包装类、字符串、集合等
        if (clazz.isPrimitive() || clazz.isArray() || clazz.isEnum()) {
            return false;
        }

        String className = clazz.getName();
        if (className.startsWith("java.") || className.startsWith("javax.")) {
            return false;
        }

        // 认为是自定义配置类
        return true;
    }

    /**
     * 生成带注释的YAML内容
     *
     * @param yaml     YamlConfiguration对象
     * @param comments 字段注释映射
     * @param header   文件头部注释
     * @return 带注释的YAML字符串
     */
    private static String generateYamlWithComments(YamlConfiguration yaml, Map<String, List<String>> comments, String header) {
        StringBuilder sb = new StringBuilder();

        // 添加文件头部注释
        if (header != null && !header.isEmpty()) {
            for (String line : header.split("\n")) {
                if (!line.trim().isEmpty()) {
                    sb.append(COMMENT_PREFIX).append(line).append("\n");
                }
            }
            sb.append("\n");
        }

        // 获取YAML字符串
        String yamlString = yaml.saveToString();
        String[] lines = yamlString.split("\n");

        // 处理每一行，在适当位置添加注释
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 计算当前行的缩进级别
            int indentLevel = getIndentLevel(line);

            // 提取键名
            String key = extractKey(line);
            if (key != null) {
                // 构建完整的字段路径
                String fieldPath = buildFieldPath(lines, i);

                // 如果该字段有注释，添加注释
                List<String> fieldComments = comments.get(fieldPath);
                if (fieldComments != null && !fieldComments.isEmpty()) {
                    // 添加空行（如果不是第一行）
                    if (sb.length() > 0 && !sb.toString().endsWith("\n\n")) {
                        sb.append("\n");
                    }

                    // 添加注释行
                    String indent = getIndent(indentLevel);
                    for (String comment : fieldComments) {
                        sb.append(indent).append(COMMENT_PREFIX).append(comment).append("\n");
                    }
                }
            }

            // 添加原始YAML行
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取行的缩进级别
     *
     * @param line YAML行
     * @return 缩进级别（以空格数计）
     */
    private static int getIndentLevel(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                spaces++;
            } else {
                break;
            }
        }
        return spaces / INDENT.length();
    }

    /**
     * 从YAML行中提取键名
     *
     * @param line YAML行
     * @return 键名，如果不是键值对则返回null
     */
    private static String extractKey(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        int colonIndex = trimmed.indexOf(':');
        if (colonIndex > 0) {
            return trimmed.substring(0, colonIndex).trim();
        }

        return null;
    }

    /**
     * 构建字段的完整路径
     *
     * @param lines      所有YAML行
     * @param currentIndex 当前行索引
     * @return 字段路径
     */
    private static String buildFieldPath(String[] lines, int currentIndex) {
        List<String> pathParts = new ArrayList<>();
        int currentIndent = getIndentLevel(lines[currentIndex]);
        String currentKey = extractKey(lines[currentIndex]);

        if (currentKey != null) {
            pathParts.add(currentKey);
        }

        // 向上查找父级键
        for (int i = currentIndex - 1; i >= 0; i--) {
            int indent = getIndentLevel(lines[i]);
            if (indent < currentIndent) {
                String key = extractKey(lines[i]);
                if (key != null) {
                    pathParts.add(0, key);
                    currentIndent = indent;
                }
            }
        }

        return String.join(".", pathParts);
    }

    /**
     * 获取指定级别的缩进字符串
     *
     * @param level 缩进级别
     * @return 缩进字符串
     */
    private static String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append(INDENT);
        }
        return sb.toString();
    }
}
