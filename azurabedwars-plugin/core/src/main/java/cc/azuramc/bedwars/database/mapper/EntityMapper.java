package cc.azuramc.bedwars.database.mapper;

import cc.azuramc.bedwars.database.annotation.Column;
import cc.azuramc.bedwars.database.annotation.QueryField;
import cc.azuramc.bedwars.database.annotation.Table;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
public final class EntityMapper {

    private static final Map<Class<?>, List<ColumnMeta>> COLUMN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> TABLE_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> QUERY_FIELD_CACHE = new ConcurrentHashMap<>();

    private EntityMapper() {
    }

    /**
     * 获取查询字段的列名（通过 @QueryField 注解的 key 查找）
     *
     * @param clazz    实体类
     * @param queryKey 查询键（@QueryField 的 value）
     * @return 对应的数据库列名
     */
    public static String getQueryColumn(Class<?> clazz, String queryKey) {
        String cacheKey = clazz.getName() + "#" + queryKey;
        return QUERY_FIELD_CACHE.computeIfAbsent(cacheKey, k -> {
            for (Field field : clazz.getDeclaredFields()) {
                QueryField[] queryFields = field.getAnnotationsByType(QueryField.class);
                for (QueryField qf : queryFields) {
                    if (qf.value().equals(queryKey)) {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null) {
                            return column.value();
                        }
                        throw new IllegalArgumentException(
                                "Field with @QueryField(\"" + queryKey + "\") must also have @Column annotation");
                    }
                }
            }
            throw new IllegalArgumentException("QueryField not found: " + queryKey + " in " + clazz.getSimpleName());
        });
    }

    /**
     * 获取查询字段的 Java 字段名（通过 @QueryField 注解的 key 查找）
     */
    public static String getQueryFieldName(Class<?> clazz, String queryKey) {
        for (Field field : clazz.getDeclaredFields()) {
            QueryField[] queryFields = field.getAnnotationsByType(QueryField.class);
            for (QueryField qf : queryFields) {
                if (qf.value().equals(queryKey)) {
                    return field.getName();
                }
            }
        }
        throw new IllegalArgumentException("QueryField not found: " + queryKey + " in " + clazz.getSimpleName());
    }

    /**
     * 获取多个查询字段的列名（通过 @QueryField 注解的 key 查找）
     */
    public static List<String> getQueryColumns(Class<?> clazz, String... queryKeys) {
        List<String> columns = new ArrayList<>();
        for (String queryKey : queryKeys) {
            columns.add(getQueryColumn(clazz, queryKey));
        }
        return columns;
    }

    /**
     * 获取表名
     */
    public static String getTableName(Class<?> clazz) {
        return TABLE_NAME_CACHE.computeIfAbsent(clazz, c -> {
            Table table = c.getAnnotation(Table.class);
            return table != null ? table.value() : c.getSimpleName().toLowerCase();
        });
    }

    /**
     * 获取所有带 @Column 注解的字段元数据
     */
    public static List<ColumnMeta> getColumnMetas(Class<?> clazz) {
        return COLUMN_CACHE.computeIfAbsent(clazz, c -> {
            List<ColumnMeta> metas = new ArrayList<>();
            for (Field field : c.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    field.setAccessible(true);
                    metas.add(new ColumnMeta(
                            field,
                            column.value(),
                            column.primaryKey(),
                            column.autoIncrement(),
                            column.type(),
                            column.defaultValue(),
                            column.size(),
                            column.nullable(),
                            column.updatable()
                    ));
                }
            }
            return Collections.unmodifiableList(metas);
        });
    }

    /**
     * 获取非主键列元数据
     */
    public static List<ColumnMeta> getNonPrimaryKeyColumns(Class<?> clazz) {
        return getColumnMetas(clazz).stream()
                .filter(meta -> !meta.primaryKey())
                .toList();
    }

    /**
     * 获取可更新列元数据（非主键且 updatable = true）
     */
    public static List<ColumnMeta> getUpdatableColumns(Class<?> clazz) {
        return getColumnMetas(clazz).stream()
                .filter(meta -> !meta.primaryKey() && meta.updatable())
                .toList();
    }

    /**
     * 获取主键列
     */
    public static ColumnMeta getPrimaryKeyColumn(Class<?> clazz) {
        return getColumnMetas(clazz).stream()
                .filter(ColumnMeta::primaryKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No primary key found for " + clazz.getSimpleName()));
    }

    /**
     * 为 INSERT 语句设置参数（排除主键）
     *
     * @return 下一个参数索引
     */
    public static int setInsertParameters(PreparedStatement ps, Object entity) throws SQLException {
        return setInsertParameters(ps, entity, 1);
    }

    /**
     * 为 INSERT 语句设置参数（排除主键）
     *
     * @param startIndex 起始参数索引（1-based）
     * @return 下一个参数索引
     */
    public static int setInsertParameters(PreparedStatement ps, Object entity, int startIndex) throws SQLException {
        List<ColumnMeta> columns = getNonPrimaryKeyColumns(entity.getClass());
        int index = startIndex;

        for (ColumnMeta meta : columns) {
            Object value = getFieldValue(meta.field(), entity);
            setParameter(ps, index++, value);
        }

        return index;
    }

    /**
     * 为 UPDATE 语句设置参数（可更新列 + 主键作为 WHERE 条件）
     */
    public static void setUpdateParameters(PreparedStatement ps, Object entity) throws SQLException {
        List<ColumnMeta> updatableColumns = getUpdatableColumns(entity.getClass());
        ColumnMeta pkColumn = getPrimaryKeyColumn(entity.getClass());

        int index = 1;

        // SET 子句参数
        for (ColumnMeta meta : updatableColumns) {
            Object value = getFieldValue(meta.field(), entity);
            setParameter(ps, index++, value);
        }

        // WHERE 子句参数（主键）
        Object pkValue = getFieldValue(pkColumn.field(), entity);
        setParameter(ps, index, pkValue);
    }

    /**
     * 设置单个参数，自动处理类型转换
     */
    public static void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else if (value instanceof UUID uuid) {
            ps.setString(index, uuid.toString());
        } else if (value instanceof Enum<?> e) {
            ps.setString(index, e.name());
        } else if (value instanceof Timestamp ts) {
            ps.setTimestamp(index, ts);
        } else {
            ps.setObject(index, value);
        }
    }

    /**
     * 实体转 Map
     */
    public static Map<String, Object> toMap(Object entity, boolean includePrimaryKey) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (ColumnMeta meta : getColumnMetas(entity.getClass())) {
            if (!includePrimaryKey && meta.primaryKey()) {
                continue;
            }
            Object value = getFieldValue(meta.field(), entity);
            // 特殊类型转换
            if (value instanceof UUID uuid) {
                value = uuid.toString();
            } else if (value instanceof Enum<?> e) {
                value = e.name();
            }
            map.put(meta.columnName(), value);
        }
        return map;
    }

    /**
     * 实体转 Document (MongoDB)
     */
    public static Document toDocument(Object entity) {
        Document doc = new Document();
        toMap(entity, false).forEach(doc::append);
        return doc;
    }

    /**
     * 实体转 UPDATE 用的 Document (MongoDB $set)
     * 排除主键和不可更新的字段
     */
    public static Document toUpdateDocument(Object entity) {
        Document setDoc = new Document();
        for (ColumnMeta meta : getColumnMetas(entity.getClass())) {
            // 排除主键和不可更新的字段
            if (meta.primaryKey() || !meta.updatable()) {
                continue;
            }
            Object value = getFieldValue(meta.field(), entity);
            if (value instanceof UUID uuid) {
                value = uuid.toString();
            } else if (value instanceof Enum<?> e) {
                value = e.name();
            }
            setDoc.append(meta.columnName(), value);
        }
        return new Document("$set", setDoc);
    }

    /**
     * ResultSet 转实体 (SQL数据库)
     */
    public static <T> T fromResultSet(ResultSet rs, Class<T> clazz, Object... constructorArgs) {
        try {
            T entity = createInstance(clazz, constructorArgs);
            for (ColumnMeta meta : getColumnMetas(clazz)) {
                Object value = rs.getObject(meta.columnName());
                setFieldValue(meta.field(), entity, value);
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to map ResultSet to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Document 转实体 (MongoDB)
     */
    public static <T> T fromDocument(Document doc, Class<T> clazz, Object... constructorArgs) {
        T entity = createInstance(clazz, constructorArgs);

        // 处理 MongoDB 的 _id
        if (doc.containsKey("_id")) {
            Field idField = findFieldByColumnName(clazz, "id");
            if (idField != null) {
                try {
                    idField.setAccessible(true);
                    Object idValue = doc.get("_id");
                    if (idValue instanceof ObjectId objectId) {
                        idField.set(entity, objectId.toString());
                    } else {
                        idField.set(entity, idValue.toString());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to set id field", e);
                }
            }
        }

        for (ColumnMeta meta : getColumnMetas(clazz)) {
            if (meta.primaryKey()) {
                // _id 已处理
                continue;
            }
            Object value = doc.get(meta.columnName());
            setFieldValue(meta.field(), entity, value);
        }
        return entity;
    }

    private static Object getFieldValue(Field field, Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<T> clazz, Object... constructorArgs) {
        try {
            if (constructorArgs == null || constructorArgs.length == 0) {
                return clazz.getDeclaredConstructor().newInstance();
            }

            Class<?>[] paramTypes = new Class<?>[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                paramTypes[i] = constructorArgs[i].getClass();
            }

            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (isAssignable(constructor.getParameterTypes(), paramTypes)) {
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(constructorArgs);
                }
            }

            throw new RuntimeException("No suitable constructor found for " + clazz.getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getSimpleName(), e);
        }
    }

    private static boolean isAssignable(Class<?>[] paramTypes, Class<?>[] argTypes) {
        if (paramTypes.length != argTypes.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (!paramTypes[i].isAssignableFrom(argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static Field findFieldByColumnName(Class<?> clazz, String columnName) {
        for (ColumnMeta meta : getColumnMetas(clazz)) {
            if (meta.columnName().equals(columnName)) {
                return meta.field();
            }
        }
        return null;
    }

    private static void setFieldValue(Field field, Object entity, Object value) {
        if (value == null) {
            return;
        }

        try {
            Class<?> fieldType = field.getType();

            if (fieldType == UUID.class && value instanceof String s) {
                value = UUID.fromString(s);
            } else if (fieldType.isEnum() && value instanceof String s) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object enumValue = Enum.valueOf((Class<Enum>) fieldType, s.toUpperCase());
                value = enumValue;
            } else if ((fieldType == double.class || fieldType == Double.class) && value instanceof Number n) {
                value = n.doubleValue();
            } else if ((fieldType == int.class || fieldType == Integer.class) && value instanceof Number n) {
                value = n.intValue();
            } else if ((fieldType == long.class || fieldType == Long.class) && value instanceof Number n) {
                value = n.longValue();
            } else if (fieldType == String.class && value instanceof Integer i) {
                value = String.valueOf(i);
            } else if (fieldType == Timestamp.class && value instanceof java.util.Date date) {
                value = new Timestamp(date.getTime());
            }

            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value: " + field.getName(), e);
        }
    }
}
